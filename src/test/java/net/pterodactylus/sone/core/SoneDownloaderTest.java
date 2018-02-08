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

import net.pterodactylus.sone.core.FreenetInterface.Fetched;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.Sone.SoneStatus;
import net.pterodactylus.sone.freenet.wot.Identity;

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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Unit test for {@link SoneDownloaderImpl} and its subclasses.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneDownloaderTest {

	private final Core core = mock(Core.class);
	private final FreenetInterface freenetInterface = mock(FreenetInterface.class);
	private final SoneParser soneParser = mock(SoneParser.class);
	private final SoneDownloaderImpl soneDownloader = new SoneDownloaderImpl(core, freenetInterface, soneParser);
	private FreenetURI requestUri = mock(FreenetURI.class);
	private Sone sone = mock(Sone.class);

	@Before
	public void setupSone() {
		Sone sone = SoneDownloaderTest.this.sone;
		Identity identity = mock(Identity.class);
		InsertableClientSSK clientSSK = createRandom(new DummyRandomSource(), "WoT");
		when(identity.getRequestUri()).thenReturn(clientSSK.getURI().toString());
		when(identity.getId()).thenReturn("identity");
		when(sone.getId()).thenReturn("identity");
		when(sone.getIdentity()).thenReturn(identity);
		requestUri = clientSSK.getURI().setKeyType("USK").setDocName("Sone");
		when(sone.getRequestUri()).thenAnswer(new Answer<FreenetURI>() {
			@Override
			public FreenetURI answer(InvocationOnMock invocation)
			throws Throwable {
				return requestUri;
			}
		});
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
		FreenetURI finalRequestUri = requestUri.sskForUSK()
				.setMetaString(new String[] { "sone.xml" });
		setupSoneAsUnknown();
		soneDownloader.fetchSoneAsSskAction(sone).run();
		verify(freenetInterface).fetchUri(finalRequestUri);
		verifyThatSoneStatusWasChangedToDownloadingAndBackTo(unknown);
		verify(core, never()).updateSone(any(Sone.class));
	}

	private void verifyThatSoneStatusWasChangedToDownloadingAndBackTo(SoneStatus soneStatus) {
		ArgumentCaptor<SoneStatus> soneStatuses = forClass(SoneStatus.class);
		verify(sone, times(2)).setStatus(soneStatuses.capture());
		assertThat(soneStatuses.getAllValues().get(0), is(downloading));
		assertThat(soneStatuses.getAllValues().get(1), is(soneStatus));
	}

	@Test
	public void notBeingAbleToFetchAKnownSoneDoesNotUpdateCore() {
		FreenetURI finalRequestUri = requestUri.sskForUSK()
				.setMetaString(new String[] { "sone.xml" });
		soneDownloader.fetchSoneAsSskAction(sone).run();
		verify(freenetInterface).fetchUri(finalRequestUri);
		verifyThatSoneStatusWasChangedToDownloadingAndBackTo(idle);
		verify(core, never()).updateSone(any(Sone.class));
	}

	@Test(expected = NullPointerException.class)
	public void exceptionWhileFetchingAnUnknownSoneDoesNotUpdateCore() {
		FreenetURI finalRequestUri = requestUri.sskForUSK()
				.setMetaString(new String[] { "sone.xml" });
		setupSoneAsUnknown();
		when(freenetInterface.fetchUri(finalRequestUri)).thenThrow(NullPointerException.class);
		try {
			soneDownloader.fetchSoneAsSskAction(sone).run();
		} finally {
			verify(freenetInterface).fetchUri(finalRequestUri);
			verifyThatSoneStatusWasChangedToDownloadingAndBackTo(unknown);
			verify(core, never()).updateSone(any(Sone.class));
		}
	}

	@Test(expected = NullPointerException.class)
	public void exceptionWhileFetchingAKnownSoneDoesNotUpdateCore() {
		FreenetURI finalRequestUri = requestUri.sskForUSK()
				.setMetaString(new String[] { "sone.xml" });
		when(freenetInterface.fetchUri(finalRequestUri)).thenThrow( NullPointerException.class);
		try {
			soneDownloader.fetchSoneAsSskAction(sone).run();
		} finally {
			verify(freenetInterface).fetchUri(finalRequestUri);
			verifyThatSoneStatusWasChangedToDownloadingAndBackTo(idle);
			verify(core, never()).updateSone(any(Sone.class));
		}
	}

	@Test
	public void fetchingSoneWithInvalidXmlWillNotUpdateTheCore() throws IOException {
		final Fetched fetchResult = createFetchResult(requestUri, getClass().getResourceAsStream("sone-parser-not-xml.xml"));
		when(freenetInterface.fetchUri(requestUri)).thenReturn(fetchResult);
		soneDownloader.fetchSoneAsSskAction(sone).run();
		verify(core, never()).updateSone(any(Sone.class));
	}

	@Test
	public void exceptionWhileFetchingSoneWillNotUpdateTheCore() throws IOException {
		final Fetched fetchResult = createFetchResult(requestUri, getClass().getResourceAsStream("sone-parser-no-payload.xml"));
		when(core.soneBuilder()).thenReturn(null);
		when(freenetInterface.fetchUri(requestUri)).thenReturn(fetchResult);
		soneDownloader.fetchSoneAsSskAction(sone).run();
		verify(core, never()).updateSone(any(Sone.class));
	}

	@Test
	public void onlyFetchingASoneWillNotUpdateTheCore() throws IOException {
		final Fetched fetchResult = createFetchResult(requestUri, getClass().getResourceAsStream("sone-parser-no-payload.xml"));
		when(freenetInterface.fetchUri(requestUri)).thenReturn(fetchResult);
		soneDownloader.fetchSone(sone, sone.getRequestUri(), true);
		verify(core, never()).updateSone(any(Sone.class));
		verifyThatSoneStatusWasChangedToDownloadingAndBackTo(idle);
	}

	private Fetched createFetchResult(FreenetURI uri, InputStream inputStream) throws IOException {
		ClientMetadata clientMetadata = new ClientMetadata("application/xml");
		Bucket bucket = mock(Bucket.class);
		when(bucket.getInputStream()).thenReturn(inputStream);
		FetchResult fetchResult = new FetchResult(clientMetadata, bucket);
		return new Fetched(uri, fetchResult);
	}

	@Test
	public void soneDownloaderCanBeCreatedByDependencyInjection() {
	    assertThat(getBaseInjector().getInstance(SoneDownloader.class), notNullValue());
	}

}
