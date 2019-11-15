package net.pterodactylus.sone.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.data.Sone;

import freenet.keys.FreenetURI;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Unit test for {@link SoneRescuer}.
 */
public class SoneRescuerTest {

	private static final long CURRENT_EDITION = 12L;
	private static final long SOME_OTHER_EDITION = 15L;
	private final Core core = mock(Core.class);
	private final SoneDownloader soneDownloader = mock(SoneDownloader.class);
	private final Sone sone = mock(Sone.class);
	private SoneRescuer soneRescuer;

	@Before
	public void setupSone() {
		FreenetURI soneUri = mock(FreenetURI.class);
		when(soneUri.getEdition()).thenReturn(CURRENT_EDITION);
		when(sone.getRequestUri()).thenReturn(soneUri);
	}

	@Before
	public void setupSoneRescuer() {
		soneRescuer = new SoneRescuer(core, soneDownloader, sone);
	}

	@Test
	public void newSoneRescuerIsNotFetchingAnything() {
		assertThat(soneRescuer.isFetching(), is(false));
	}

	@Test
	public void newSoneRescuerStartsAtCurrentEditionOfSone() {
		assertThat(soneRescuer.getCurrentEdition(), is(CURRENT_EDITION));
	}

	@Test
	public void newSoneRescuerHasANextEditionToGet() {
		assertThat(soneRescuer.hasNextEdition(), is(true));
	}

	@Test
	public void soneRescuerDoesNotHaveANextEditionIfCurrentEditionIsZero() {
		when(sone.getRequestUri().getEdition()).thenReturn(0L);
		soneRescuer = new SoneRescuer(core, soneDownloader, sone);
		assertThat(soneRescuer.hasNextEdition(), is(false));
	}

	@Test
	public void nextEditionIsOneSmallerThanTheCurrentEdition() {
		assertThat(soneRescuer.getNextEdition(), is(CURRENT_EDITION - 1));
	}

	@Test
	public void currentEditionCanBeSet() {
		soneRescuer.setEdition(SOME_OTHER_EDITION);
		assertThat(soneRescuer.getCurrentEdition(), is(SOME_OTHER_EDITION));
	}

	@Test
	public void lastFetchOfANewSoneRescuerWasSuccessful() {
		assertThat(soneRescuer.isLastFetchSuccessful(), is(true));
	}

	@Test
	public void mainLoopStopsWhenItShould() {
		soneRescuer.stop();
		soneRescuer.serviceRun();
	}

	@Test
	public void successfulInsert() {
		final Sone fetchedSone = mock(Sone.class);
		returnUriOnInsert(fetchedSone);
		soneRescuer.startNextFetch();
		soneRescuer.serviceRun();
		verify(core).lockSone(eq(sone));
		verify(core).updateSone(eq(fetchedSone), eq(true));
		assertThat(soneRescuer.isLastFetchSuccessful(), is(true));
		assertThat(soneRescuer.isFetching(), is(false));
	}

	@Test
	public void nonSuccessfulInsertIsRecognized() {
		returnUriOnInsert(null);
		soneRescuer.startNextFetch();
		soneRescuer.serviceRun();
		verify(core).lockSone(eq(sone));
		verify(core, never()).updateSone(any(Sone.class), eq(true));
		assertThat(soneRescuer.isLastFetchSuccessful(), is(false));
		assertThat(soneRescuer.isFetching(), is(false));
	}

	private void returnUriOnInsert(final Sone fetchedSone) {
		FreenetURI keyWithMetaStrings = setupFreenetUri();
		doAnswer(new Answer<Sone>() {
			@Override
			public Sone answer(InvocationOnMock invocation) throws Throwable {
				soneRescuer.stop();
				return fetchedSone;
			}
		}).when(soneDownloader).fetchSone(eq(sone), eq(keyWithMetaStrings), eq(true));
	}

	private FreenetURI setupFreenetUri() {
		FreenetURI sskKey = mock(FreenetURI.class);
		FreenetURI keyWithDocName = mock(FreenetURI.class);
		FreenetURI keyWithMetaStrings = mock(FreenetURI.class);
		when(keyWithDocName.setMetaString(eq(new String[] { "sone.xml" }))).thenReturn(keyWithMetaStrings);
		when(sskKey.setDocName(eq("Sone-" + CURRENT_EDITION))).thenReturn(keyWithDocName);
		when(sone.getRequestUri().setKeyType(eq("SSK"))).thenReturn(sskKey);
		return keyWithMetaStrings;
	}

}
