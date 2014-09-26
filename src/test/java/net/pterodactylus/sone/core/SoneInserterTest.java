package net.pterodactylus.sone.core;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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

import com.google.common.base.Optional;
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
		when(core.getSone(anyString())).thenReturn(Optional.<Sone>absent());
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
		SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, "SoneId");
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
		when(core.getSone(anyString())).thenReturn(of(sone));
		return sone;
	}

	@Test
	public void isModifiedIsTrueIfModificationDetectorSaysSo() {
		SoneModificationDetector soneModificationDetector = mock(SoneModificationDetector.class);
		when(soneModificationDetector.isModified()).thenReturn(true);
		SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, "SoneId", soneModificationDetector, 1);
		assertThat(soneInserter.isModified(), is(true));
	}

	@Test
	public void isModifiedIsFalseIfModificationDetectorSaysSo() {
		SoneModificationDetector soneModificationDetector = mock(SoneModificationDetector.class);
		SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, "SoneId", soneModificationDetector, 1);
		assertThat(soneInserter.isModified(), is(false));
	}

	@Test
	public void lastFingerprintIsStoredCorrectly() {
		SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, "SoneId");
		soneInserter.setLastInsertFingerprint("last-fingerprint");
		assertThat(soneInserter.getLastInsertFingerprint(), is("last-fingerprint"));
	}

	@Test
	public void soneInserterStopsWhenItShould() {
		SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, "SoneId");
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
		final SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, "SoneId", soneModificationDetector, 1);
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
		final SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, "SoneId", soneModificationDetector, 1);
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
		final SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, "SoneId", soneModificationDetector, 1);
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
		final SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, "SoneId", soneModificationDetector, 1);
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
