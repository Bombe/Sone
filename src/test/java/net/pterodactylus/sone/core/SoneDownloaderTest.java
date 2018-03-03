package net.pterodactylus.sone.core;

import static freenet.keys.InsertableClientSSK.createRandom;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.DAYS;
import static net.pterodactylus.sone.data.Sone.SoneStatus.downloading;
import static net.pterodactylus.sone.data.Sone.SoneStatus.idle;
import static net.pterodactylus.sone.data.Sone.SoneStatus.unknown;
import static net.pterodactylus.sone.web.AllPagesTestKt.getBaseInjector;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.Sone.SoneStatus;
import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.test.GuiceKt;

import freenet.client.ClientMetadata;
import freenet.client.FetchResult;
import freenet.client.async.USKCallback;
import freenet.crypt.DummyRandomSource;
import freenet.keys.FreenetURI;
import freenet.keys.InsertableClientSSK;
import freenet.support.api.Bucket;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Unit test for {@link SoneDownloaderImpl} and its subclasses.
 */
public class SoneDownloaderTest {

	private final FreenetInterface freenetInterface = mock(FreenetInterface.class);
	private final SoneParser soneParser = mock(SoneParser.class);
	private final UpdatedSoneProcessor updatedSoneProcessor = mock(UpdatedSoneProcessor.class);
	private final SoneDownloaderImpl soneDownloader = new SoneDownloaderImpl(updatedSoneProcessor, freenetInterface, soneParser);
	private final InsertableClientSSK clientSSK = createRandom(new DummyRandomSource(), "WoT");
	private final FreenetURI requestUri = clientSSK.getURI().setKeyType("USK").setDocName("Sone");
	private final FreenetURI finalRequestUri = requestUri.setMetaString(new String[] { "sone.xml" });
	private final Sone sone = mock(Sone.class);
	private final Sone parsedSone = mock(Sone.class);

	@Before
	public void setupSone() {
		Identity identity = mock(Identity.class);
		when(identity.getRequestUri()).thenReturn(clientSSK.getURI().toString());
		when(identity.getId()).thenReturn("identity");
		when(sone.getId()).thenReturn("identity");
		when(sone.getIdentity()).thenReturn(identity);
		when(sone.getRequestUri()).thenReturn(requestUri);
		when(sone.getTime()).thenReturn(currentTimeMillis() - DAYS.toMillis(1));
	}

	private void setupSoneAsUnknown() {
		when(sone.getTime()).thenReturn(0L);
	}

	@Test
	public void addingASoneWillRegisterItsKey() {
		soneDownloader.addSone(sone);
		verify(freenetInterface).registerActiveUsk(eq(sone.getRequestUri()), any(
				USKCallback.class));
		verify(freenetInterface, never()).unregisterUsk(sone);
	}

	@Test
	public void addingASoneTwiceWillAlsoDeregisterItsKey() {
		soneDownloader.addSone(sone);
		soneDownloader.addSone(sone);
		verify(freenetInterface, times(2)).registerActiveUsk(eq(
				sone.getRequestUri()), any(USKCallback.class));
		verify(freenetInterface).unregisterUsk(sone);
	}


	@Test
	public void stoppingTheSoneDownloaderUnregistersTheSone() {
		soneDownloader.addSone(sone);
		soneDownloader.stop();
		verify(freenetInterface).unregisterUsk(sone);
	}

	@Test
	public void notBeingAbleToFetchAnUnknownSoneDoesNotUpdateCore() {
		setupSoneAsUnknown();
		soneDownloader.fetchSoneAsSskAction(sone).run();
		verify(freenetInterface).fetchUri(finalRequestUri.sskForUSK());
		verifyThatSoneStatusWasChangedToDownloadingAndBackTo(unknown);
		verify(updatedSoneProcessor, never()).updateSone(any(Sone.class));
	}

	private void verifyThatSoneStatusWasChangedToDownloadingAndBackTo(SoneStatus soneStatus) {
		ArgumentCaptor<SoneStatus> soneStatuses = forClass(SoneStatus.class);
		verify(sone, times(2)).setStatus(soneStatuses.capture());
		assertThat(soneStatuses.getAllValues().get(0), is(downloading));
		assertThat(soneStatuses.getAllValues().get(1), is(soneStatus));
	}

	@Test
	public void notBeingAbleToFetchAKnownSoneDoesNotUpdateCore() {
		soneDownloader.fetchSoneAsSskAction(sone).run();
		verify(freenetInterface).fetchUri(finalRequestUri.sskForUSK());
		verifyThatSoneStatusWasChangedToDownloadingAndBackTo(idle);
		verify(updatedSoneProcessor, never()).updateSone(any(Sone.class));
	}

	@Test(expected = NullPointerException.class)
	public void exceptionWhileFetchingSoneDoesNotProcessUpdatedSone() {
		when(freenetInterface.fetchUri(any(FreenetURI.class))).thenThrow(NullPointerException.class);
		try {
			soneDownloader.fetchSoneAsSskAction(sone).run();
		} finally {
			verify(updatedSoneProcessor, never()).updateSone(any(Sone.class));
		}
	}

	@Test
	public void onlyFetchingASoneWillNotUpdateTheCore() throws IOException, SoneException {
		setupParsedSone();
		soneDownloader.fetchSone(sone, sone.getRequestUri(), true);
		verify(updatedSoneProcessor, never()).updateSone(any(Sone.class));
		verifyThatSoneStatusWasChangedToDownloadingAndBackTo(idle);
	}

	@Test
	public void fetchingACompleteSoneNotifiesTheUpdatedSoneProcessor() throws IOException, SoneException {
		setupParsedSone();
		soneDownloader.fetchSone(sone, sone.getRequestUri(), false);
		verify(updatedSoneProcessor).updateSone(parsedSone);
		verifyThatSoneStatusWasChangedToDownloadingAndBackTo(idle);
	}

	private void setupParsedSone() throws IOException, SoneException {
		InputStream inputStream = mock(InputStream.class);
		ClientMetadata clientMetadata = new ClientMetadata("application/xml");
		Bucket bucket = mock(Bucket.class);
		when(bucket.getInputStream()).thenReturn(inputStream);
		FetchResult fetchResult = new FetchResult(clientMetadata, bucket);
		Fetched fetched = new Fetched(finalRequestUri, fetchResult);
		when(freenetInterface.fetchUri(eq(finalRequestUri))).thenReturn(fetched);
		when(soneParser.parseSone(sone, inputStream)).thenReturn(parsedSone);
	}

	@Test
	public void soneDownloaderCanBeCreatedByDependencyInjection() {
		assertThat(getBaseInjector().createChildInjector(
				GuiceKt.supply(UpdatedSoneProcessor.class).byInstance(mock(UpdatedSoneProcessor.class)),
				GuiceKt.supply(SoneParser.class).byInstance(mock(SoneParser.class))
		).getInstance(SoneDownloader.class), notNullValue());
	}

}
