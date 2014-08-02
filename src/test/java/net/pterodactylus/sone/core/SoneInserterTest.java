package net.pterodactylus.sone.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import net.pterodactylus.sone.core.SoneInserter.InsertInformation;
import net.pterodactylus.sone.core.SoneInserter.SetInsertionDelay;
import net.pterodactylus.sone.core.event.SoneEvent;
import net.pterodactylus.sone.core.event.SoneInsertAbortedEvent;
import net.pterodactylus.sone.core.event.SoneInsertedEvent;
import net.pterodactylus.sone.core.event.SoneInsertingEvent;
import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Sone;

import freenet.keys.FreenetURI;

import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Unit test for {@link SoneInserter} and its subclasses.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneInserterTest {

	private final Core core = mock(Core.class);
	private final EventBus eventBus = mock(EventBus.class);
	private final FreenetInterface freenetInterface = mock(FreenetInterface.class);

	@Before
	public void setupCore() {
		UpdateChecker updateChecker = mock(UpdateChecker.class);
		when(core.getUpdateChecker()).thenReturn(updateChecker);
	}

	@Test
	public void insertionDelayIsForwardedToSoneInserter() {
		SetInsertionDelay setInsertionDelay = new SetInsertionDelay();
		setInsertionDelay.optionChanged(null, null, 15);
		assertThat(SoneInserter.getInsertionDelay().get(), is(15));
	}

	@Test
	/* this test is hilariously bad. */
	public void manifestEntriesAreCreated() {
		FreenetURI insertUri = mock(FreenetURI.class);
		String fingerprint = "fingerprint";
		Sone sone = createSone(insertUri, fingerprint);
		SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, sone);
		InsertInformation insertInformation = soneInserter.new InsertInformation(sone);
		HashMap<String, Object> manifestEntries = insertInformation.generateManifestEntries();
		assertThat(manifestEntries.keySet(), containsInAnyOrder("index.html", "sone.xml"));
		assertThat(insertInformation.getInsertUri(), is(insertUri));
		assertThat(insertInformation.getFingerprint(), is(fingerprint));
	}

	private Sone createSone(FreenetURI insertUri, String fingerprint) {
		Sone sone = mock(Sone.class);
		when(sone.getInsertUri()).thenReturn(insertUri);
		when(sone.getFingerprint()).thenReturn(fingerprint);
		when(sone.getRootAlbum()).thenReturn(mock(Album.class));
		return sone;
	}

	@Test(expected = IllegalArgumentException.class)
	public void soneOfSoneInserterCanNotBeSetToADifferentSone() {
		Sone sone = mock(Sone.class);
		SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, sone);
		soneInserter.setSone(mock(Sone.class));
	}

	@Test
	public void soneCanBeSetToEqualSone() {
		Sone sone = mock(Sone.class);
		SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, sone);
		soneInserter.setSone(sone);
	}

	@Test
	public void isModifiedIsTrueIfModificationDetectorSaysSo() {
		Sone sone = mock(Sone.class);
		SoneModificationDetector soneModificationDetector = mock(SoneModificationDetector.class);
		when(soneModificationDetector.isModified()).thenReturn(true);
		SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, sone, soneModificationDetector);
		assertThat(soneInserter.isModified(), is(true));
	}

	@Test
	public void isModifiedIsFalseIfModificationDetectorSaysSo() {
		Sone sone = mock(Sone.class);
		SoneModificationDetector soneModificationDetector = mock(SoneModificationDetector.class);
		SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, sone, soneModificationDetector);
		assertThat(soneInserter.isModified(), is(false));
	}

	@Test
	public void lastFingerprintIsStoredCorrectly() {
		Sone sone = mock(Sone.class);
		SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, sone);
		soneInserter.setLastInsertFingerprint("last-fingerprint");
		assertThat(soneInserter.getLastInsertFingerprint(), is("last-fingerprint"));
	}

	@Test
	public void soneInserterStopsWhenItShould() {
		Sone sone = mock(Sone.class);
		SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, sone);
		soneInserter.stop();
		soneInserter.serviceRun();
	}

	@Test
	public void soneInserterInsertsASoneIfItIsEligible() throws SoneException {
		FreenetURI insertUri = mock(FreenetURI.class);
		final FreenetURI finalUri = mock(FreenetURI.class);
		String fingerprint = "fingerprint";
		Sone sone = createSone(insertUri, fingerprint);
		SoneModificationDetector soneModificationDetector = mock(SoneModificationDetector.class);
		when(soneModificationDetector.isEligibleForInsert()).thenReturn(true);
		when(freenetInterface.insertDirectory(eq(insertUri), any(HashMap.class), eq("index.html"))).thenReturn(finalUri);
		final SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, sone, soneModificationDetector);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				soneInserter.stop();
				return null;
			}
		}).when(core).touchConfiguration();
		soneInserter.serviceRun();
		ArgumentCaptor<SoneEvent> soneEvents = ArgumentCaptor.forClass(SoneEvent.class);
		verify(freenetInterface).insertDirectory(eq(insertUri), any(HashMap.class), eq("index.html"));
		verify(eventBus, times(2)).post(soneEvents.capture());
		assertThat(soneEvents.getAllValues().get(0), instanceOf(SoneInsertingEvent.class));
		assertThat(soneEvents.getAllValues().get(0).sone(), is(sone));
		assertThat(soneEvents.getAllValues().get(1), instanceOf(SoneInsertedEvent.class));
		assertThat(soneEvents.getAllValues().get(1).sone(), is(sone));
	}

	@Test
	public void soneInserterBailsOutIfItIsStoppedWhileInserting() throws SoneException {
		FreenetURI insertUri = mock(FreenetURI.class);
		final FreenetURI finalUri = mock(FreenetURI.class);
		String fingerprint = "fingerprint";
		Sone sone = createSone(insertUri, fingerprint);
		SoneModificationDetector soneModificationDetector = mock(SoneModificationDetector.class);
		when(soneModificationDetector.isEligibleForInsert()).thenReturn(true);
		final SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, sone, soneModificationDetector);
		when(freenetInterface.insertDirectory(eq(insertUri), any(HashMap.class), eq("index.html"))).thenAnswer(new Answer<FreenetURI>() {
			@Override
			public FreenetURI answer(InvocationOnMock invocation) throws Throwable {
				soneInserter.stop();
				return finalUri;
			}
		});
		soneInserter.serviceRun();
		ArgumentCaptor<SoneEvent> soneEvents = ArgumentCaptor.forClass(SoneEvent.class);
		verify(freenetInterface).insertDirectory(eq(insertUri), any(HashMap.class), eq("index.html"));
		verify(eventBus, times(2)).post(soneEvents.capture());
		assertThat(soneEvents.getAllValues().get(0), instanceOf(SoneInsertingEvent.class));
		assertThat(soneEvents.getAllValues().get(0).sone(), is(sone));
		assertThat(soneEvents.getAllValues().get(1), instanceOf(SoneInsertedEvent.class));
		assertThat(soneEvents.getAllValues().get(1).sone(), is(sone));
		verify(core, never()).touchConfiguration();
	}

	@Test
	public void soneInserterDoesNotInsertSoneIfItIsNotEligible() throws SoneException {
		FreenetURI insertUri = mock(FreenetURI.class);
		String fingerprint = "fingerprint";
		Sone sone = createSone(insertUri, fingerprint);
		SoneModificationDetector soneModificationDetector = mock(SoneModificationDetector.class);
		final SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, sone, soneModificationDetector);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException ie1) {
					throw new RuntimeException(ie1);
				}
				soneInserter.stop();
			}
		}).start();
		soneInserter.serviceRun();
		verify(freenetInterface, never()).insertDirectory(eq(insertUri), any(HashMap.class), eq("index.html"));
		verify(eventBus, never()).post(argThat(org.hamcrest.Matchers.any(SoneEvent.class)));
	}

	@Test
	public void soneInserterPostsAbortedEventIfAnExceptionOccurs() throws SoneException {
		FreenetURI insertUri = mock(FreenetURI.class);
		String fingerprint = "fingerprint";
		Sone sone = createSone(insertUri, fingerprint);
		SoneModificationDetector soneModificationDetector = mock(SoneModificationDetector.class);
		when(soneModificationDetector.isEligibleForInsert()).thenReturn(true);
		final SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, sone, soneModificationDetector);
		final SoneException soneException = new SoneException(new Exception());
		when(freenetInterface.insertDirectory(eq(insertUri), any(HashMap.class), eq("index.html"))).thenAnswer(new Answer<FreenetURI>() {
			@Override
			public FreenetURI answer(InvocationOnMock invocation) throws Throwable {
				soneInserter.stop();
				throw soneException;
			}
		});
		soneInserter.serviceRun();
		ArgumentCaptor<SoneEvent> soneEvents = ArgumentCaptor.forClass(SoneEvent.class);
		verify(freenetInterface).insertDirectory(eq(insertUri), any(HashMap.class), eq("index.html"));
		verify(eventBus, times(2)).post(soneEvents.capture());
		assertThat(soneEvents.getAllValues().get(0), instanceOf(SoneInsertingEvent.class));
		assertThat(soneEvents.getAllValues().get(0).sone(), is(sone));
		assertThat(soneEvents.getAllValues().get(1), instanceOf(SoneInsertAbortedEvent.class));
		assertThat(soneEvents.getAllValues().get(1).sone(), is(sone));
		verify(core, never()).touchConfiguration();
	}

}
