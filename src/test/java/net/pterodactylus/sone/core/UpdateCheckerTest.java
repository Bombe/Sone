package net.pterodactylus.sone.core;

import static java.lang.Long.MAX_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.io.IOException;
import java.io.InputStream;

import net.pterodactylus.sone.core.FreenetInterface.Callback;
import net.pterodactylus.sone.core.event.UpdateFoundEvent;
import net.pterodactylus.util.version.Version;

import freenet.client.ClientMetadata;
import freenet.client.FetchResult;
import freenet.keys.FreenetURI;
import freenet.support.api.Bucket;
import freenet.support.io.ArrayBucket;

import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Unit test for {@link UpdateChecker}.
 */
public class UpdateCheckerTest {

	private final EventBus eventBus = mock(EventBus.class);
	private final FreenetInterface freenetInterface = mock(FreenetInterface.class);
	private final Version currentVersion = new Version(1, 0, 0);
	private final UpdateChecker updateChecker = new UpdateChecker(eventBus, freenetInterface, currentVersion);

	@Before
	public void startUpdateChecker() {
		updateChecker.start();
	}

	@Test
	public void newUpdateCheckerDoesNotHaveALatestVersion() {
		assertThat(updateChecker.hasLatestVersion(), is(false));
		assertThat(updateChecker.getLatestVersion(), is(currentVersion));
	}

	@Test
	public void startingAnUpdateCheckerRegisterAUsk() {
		verify(freenetInterface).registerUsk(any(FreenetURI.class), any(Callback.class));
	}

	@Test
	public void stoppingAnUpdateCheckerUnregistersAUsk() {
		updateChecker.stop();
		verify(freenetInterface).unregisterUsk(any(FreenetURI.class));
	}

	@Test
	public void callbackDoesNotDownloadIfNewEditionIsNotFound() {
		setupCallbackWithEdition(MAX_VALUE, false, false);
		verify(freenetInterface, never()).fetchUri(any(FreenetURI.class));
		verify(eventBus, never()).post(argThat(instanceOf(UpdateFoundEvent.class)));
	}

	private void setupCallbackWithEdition(long edition, boolean newKnownGood, boolean newSlot) {
		ArgumentCaptor<FreenetURI> uri = forClass(FreenetURI.class);
		ArgumentCaptor<Callback> callback = forClass(Callback.class);
		verify(freenetInterface).registerUsk(uri.capture(), callback.capture());
		callback.getValue().editionFound(uri.getValue(), edition, newKnownGood, newSlot);
	}

	@Test
	public void callbackStartsIfNewEditionIsFound() {
		setupFetchResult(createFutureFetchResult());
		setupCallbackWithEdition(MAX_VALUE, true, false);
		verifyAFreenetUriIsFetched();
		verifyEventIsFired(new Version(99, 0, 0), 11865368297000L, false);
		verifyThatUpdateCheckerKnowsLatestVersion(new Version(99, 0, 0), 11865368297000L);
	}

	private FetchResult createFutureFetchResult() {
		ClientMetadata clientMetadata = new ClientMetadata("application/xml");
		Bucket fetched = new ArrayBucket(("# MapConfigurationBackendVersion=1\n" +
				"CurrentVersion/Version: 99.0.0\n" +
				"CurrentVersion/ReleaseTime: 11865368297000\n" +
				"DisruptiveVersion/0.1.2: true").getBytes());
		return new FetchResult(clientMetadata, fetched);
	}

	private void verifyEventIsFired(Version version, long releaseTime, boolean disruptive) {
		ArgumentCaptor<UpdateFoundEvent> updateFoundEvent = forClass(UpdateFoundEvent.class);
		verify(eventBus, times(1)).post(updateFoundEvent.capture());
		assertThat(updateFoundEvent.getValue().version(), is(version));
		assertThat(updateFoundEvent.getValue().releaseTime(), is(releaseTime));
		assertThat(updateFoundEvent.getValue().disruptive(), is(disruptive));
	}

	private void verifyThatUpdateCheckerKnowsLatestVersion(Version version, long releaseTime) {
		assertThat(updateChecker.getLatestVersion(), is(version));
		assertThat(updateChecker.getLatestVersionDate(), is(releaseTime));
		assertThat(updateChecker.hasLatestVersion(), is(true));
	}

	@Test
	public void callbackDoesNotStartIfNoNewEditionIsFound() {
		setupFetchResult(createPastFetchResult());
		setupCallbackWithEdition(updateChecker.getLatestEdition(), true, false);
		verifyAFreenetUriIsFetched();
		verifyNoUpdateFoundEventIsFired();
	}

	private void setupFetchResult(final FetchResult pastFetchResult) {
		when(freenetInterface.fetchUri(any(FreenetURI.class))).thenAnswer(new Answer<Fetched>() {
			@Override
			public Fetched answer(InvocationOnMock invocation) throws Throwable {
				FreenetURI freenetUri = (FreenetURI) invocation.getArguments()[0];
				return new Fetched(freenetUri, pastFetchResult);
			}
		});
	}

	private FetchResult createPastFetchResult() {
		ClientMetadata clientMetadata = new ClientMetadata("application/xml");
		Bucket fetched = new ArrayBucket(("# MapConfigurationBackendVersion=1\n" +
				"CurrentVersion/Version: 0.2\n" +
				"CurrentVersion/ReleaseTime: 1289417883000").getBytes());
		return new FetchResult(clientMetadata, fetched);
	}

	@Test
	public void invalidUpdateFileDoesNotStartCallback() {
		setupFetchResult(createInvalidFetchResult());
		setupCallbackWithEdition(MAX_VALUE, true, false);
		verifyAFreenetUriIsFetched();
		verifyNoUpdateFoundEventIsFired();
	}

	private FetchResult createInvalidFetchResult() {
		ClientMetadata clientMetadata = new ClientMetadata("text/plain");
		Bucket fetched = new ArrayBucket("Some other data.".getBytes());
		return new FetchResult(clientMetadata, fetched);
	}

	@Test
	public void nonExistingPropertiesWillNotCauseUpdateToBeFound() {
		setupCallbackWithEdition(MAX_VALUE, true, false);
		verifyAFreenetUriIsFetched();
		verifyNoUpdateFoundEventIsFired();
	}

	private void verifyNoUpdateFoundEventIsFired() {
		verify(eventBus, never()).post(any(UpdateFoundEvent.class));
	}

	private void verifyAFreenetUriIsFetched() {
		verify(freenetInterface).fetchUri(any(FreenetURI.class));
	}

	@Test
	public void brokenBucketDoesNotCauseUpdateToBeFound() {
		setupFetchResult(createBrokenBucketFetchResult());
		setupCallbackWithEdition(MAX_VALUE, true, false);
		verifyAFreenetUriIsFetched();
		verifyNoUpdateFoundEventIsFired();
	}

	private FetchResult createBrokenBucketFetchResult() {
		ClientMetadata clientMetadata = new ClientMetadata("text/plain");
		Bucket fetched = new ArrayBucket("Some other data.".getBytes()) {
			@Override
			public InputStream getInputStream() {
				try {
					return when(mock(InputStream.class).read()).thenThrow(IOException.class).getMock();
				} catch (IOException ioe1) {
					/* won’t throw here. */
					return null;
				}
			}
		};
		return new FetchResult(clientMetadata, fetched);
	}

	@Test
	public void invalidTimeDoesNotCauseAnUpdateToBeFound() {
		setupFetchResult(createInvalidTimeFetchResult());
		setupCallbackWithEdition(MAX_VALUE, true, false);
		verifyAFreenetUriIsFetched();
		verifyNoUpdateFoundEventIsFired();
	}

	private FetchResult createInvalidTimeFetchResult() {
		ClientMetadata clientMetadata = new ClientMetadata("application/xml");
		Bucket fetched = new ArrayBucket(("# MapConfigurationBackendVersion=1\n" +
				"CurrentVersion/Version: 0.2\n" +
				"CurrentVersion/ReleaseTime: invalid").getBytes());
		return new FetchResult(clientMetadata, fetched);
	}

	@Test
	public void invalidPropertiesDoesNotCauseAnUpdateToBeFound() {
		setupFetchResult(createMissingTimeFetchResult());
		setupCallbackWithEdition(MAX_VALUE, true, false);
		verifyAFreenetUriIsFetched();
		verifyNoUpdateFoundEventIsFired();
	}

	private FetchResult createMissingTimeFetchResult() {
		ClientMetadata clientMetadata = new ClientMetadata("application/xml");
		Bucket fetched = new ArrayBucket(("# MapConfigurationBackendVersion=1\n" +
				"CurrentVersion/Version: 0.2\n").getBytes());
		return new FetchResult(clientMetadata, fetched);
	}

	@Test
	public void invalidVersionDoesNotCauseAnUpdateToBeFound() {
		setupFetchResult(createInvalidVersionFetchResult());
		setupCallbackWithEdition(MAX_VALUE, true, false);
		verifyAFreenetUriIsFetched();
		verifyNoUpdateFoundEventIsFired();
	}

	private FetchResult createInvalidVersionFetchResult() {
		ClientMetadata clientMetadata = new ClientMetadata("application/xml");
		Bucket fetched = new ArrayBucket(("# MapConfigurationBackendVersion=1\n" +
				"CurrentVersion/Version: foo\n" +
				"CurrentVersion/ReleaseTime: 1289417883000").getBytes());
		return new FetchResult(clientMetadata, fetched);
	}

	@Test
	public void disruptiveVersionGetsNotification() {
		setupFetchResult(createDisruptiveVersionFetchResult());
		setupCallbackWithEdition(MAX_VALUE, true, false);
		verifyAFreenetUriIsFetched();
		verifyEventIsFired(new Version(1, 2, 3), 1289417883000L, true);
		verifyThatUpdateCheckerKnowsLatestVersion(new Version(1, 2, 3), 1289417883000L);
	}

	private FetchResult createDisruptiveVersionFetchResult() {
		ClientMetadata clientMetadata = new ClientMetadata("application/xml");
		Bucket fetched = new ArrayBucket(("# MapConfigurationBackendVersion=1\n" +
				"CurrentVersion/Version: 1.2.3\n" +
				"CurrentVersion/ReleaseTime: 1289417883000\n" +
				"DisruptiveVersion/1.2.3: true").getBytes());
		return new FetchResult(clientMetadata, fetched);
	}

}
