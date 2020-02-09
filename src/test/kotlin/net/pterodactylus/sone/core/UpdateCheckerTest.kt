package net.pterodactylus.sone.core

import com.google.common.eventbus.*
import freenet.client.*
import freenet.keys.*
import freenet.support.io.*
import net.pterodactylus.sone.core.FreenetInterface.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.util.version.Version
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.junit.*
import org.mockito.ArgumentCaptor.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.hamcrest.MockitoHamcrest.*
import java.io.*
import kotlin.Long.Companion.MAX_VALUE

/**
 * Unit test for [UpdateChecker].
 */
class UpdateCheckerTest {

	private val eventBus = mock<EventBus>()
	private val freenetInterface = mock<FreenetInterface>()
	private val currentVersion = Version(1, 0, 0)
	private val pluginHomepage = PluginHomepage("KSK@homepage")
	private val updateChecker = UpdateChecker(eventBus, freenetInterface, currentVersion, pluginHomepage)

	@Before
	fun startUpdateChecker() {
		updateChecker.start()
	}

	@Test
	fun `new update checker does not have a latest version`() {
		assertThat(updateChecker.hasLatestVersion(), equalTo(false))
		assertThat(updateChecker.latestVersion, equalTo(currentVersion))
	}

	@Test
	fun `starting an update checker register a usk`() {
		verify(freenetInterface).registerUsk(any(FreenetURI::class.java), any(Callback::class.java))
	}

	@Test
	fun `stopping an update checker unregisters a usk`() {
		updateChecker.stop()
		verify(freenetInterface).unregisterUsk(any(FreenetURI::class.java))
	}

	@Test
	fun `callback does not download if new edition is not found`() {
		setupCallbackWithEdition(MAX_VALUE, false)
		verify(freenetInterface, never()).fetchUri(any(FreenetURI::class.java))
		verify(eventBus, never()).post(argThat(instanceOf(UpdateFoundEvent::class.java)))
	}

	private fun setupCallbackWithEdition(edition: Long, newKnownGood: Boolean, newSlot: Boolean = false) {
		val uri = forClass(FreenetURI::class.java)
		val callback = forClass(Callback::class.java)
		verify(freenetInterface).registerUsk(uri.capture(), callback.capture())
		callback.value.editionFound(uri.value, edition, newKnownGood, newSlot)
	}

	@Test
	fun `callback starts if new edition is found`() {
		setupFetchResult(createFutureFetchResult())
		setupCallbackWithEdition(MAX_VALUE, true)
		verifyAFreenetUriIsFetched()
		verifyEventIsFired(Version(99, 0, 0), 11865368297000L, false)
		verifyThatUpdateCheckerKnowsLatestVersion(Version(99, 0, 0), 11865368297000L)
	}

	private fun createFutureFetchResult(): FetchResult {
		val clientMetadata = ClientMetadata("application/xml")
		val fetched = ArrayBucket(("# MapConfigurationBackendVersion=1\n" +
				"CurrentVersion/Version: 99.0.0\n" +
				"CurrentVersion/ReleaseTime: 11865368297000\n" +
				"DisruptiveVersion/0.1.2: true").toByteArray())
		return FetchResult(clientMetadata, fetched)
	}

	private fun verifyEventIsFired(version: Version, releaseTime: Long, disruptive: Boolean) {
		val updateFoundEvent = forClass(UpdateFoundEvent::class.java)
		verify(eventBus, times(1)).post(updateFoundEvent.capture())
		assertThat(updateFoundEvent.value.version, equalTo(version))
		assertThat(updateFoundEvent.value.releaseTime, equalTo(releaseTime))
		assertThat(updateFoundEvent.value.isDisruptive, equalTo(disruptive))
	}

	private fun verifyThatUpdateCheckerKnowsLatestVersion(version: Version, releaseTime: Long) {
		assertThat(updateChecker.latestVersion, equalTo(version))
		assertThat(updateChecker.latestVersionDate, equalTo(releaseTime))
		assertThat(updateChecker.hasLatestVersion(), equalTo(true))
	}

	@Test
	fun `callback does not start if no new edition is found`() {
		setupFetchResult(createPastFetchResult())
		setupCallbackWithEdition(updateChecker.latestEdition, true)
		verifyAFreenetUriIsFetched()
		verifyNoUpdateFoundEventIsFired()
	}

	private fun setupFetchResult(pastFetchResult: FetchResult) {
		whenever(freenetInterface.fetchUri(any(FreenetURI::class.java))).thenAnswer { invocation ->
			val freenetUri = invocation.arguments[0] as FreenetURI
			Fetched(freenetUri, pastFetchResult)
		}
	}

	private fun createPastFetchResult(): FetchResult {
		val clientMetadata = ClientMetadata("application/xml")
		val fetched = ArrayBucket(("# MapConfigurationBackendVersion=1\n" +
				"CurrentVersion/Version: 0.2\n" +
				"CurrentVersion/ReleaseTime: 1289417883000").toByteArray())
		return FetchResult(clientMetadata, fetched)
	}

	@Test
	fun `invalid update file does not start callback`() {
		setupFetchResult(createInvalidFetchResult())
		setupCallbackWithEdition(MAX_VALUE, true)
		verifyAFreenetUriIsFetched()
		verifyNoUpdateFoundEventIsFired()
	}

	private fun createInvalidFetchResult(): FetchResult {
		val clientMetadata = ClientMetadata("text/plain")
		val fetched = ArrayBucket("Some other data.".toByteArray())
		return FetchResult(clientMetadata, fetched)
	}

	@Test
	fun `non existing properties will not cause update to be found`() {
		setupCallbackWithEdition(MAX_VALUE, true)
		verifyAFreenetUriIsFetched()
		verifyNoUpdateFoundEventIsFired()
	}

	private fun verifyNoUpdateFoundEventIsFired() {
		verify(eventBus, never()).post(any(UpdateFoundEvent::class.java))
	}

	private fun verifyAFreenetUriIsFetched() {
		verify(freenetInterface).fetchUri(any(FreenetURI::class.java))
	}

	@Test
	fun `broken bucket does not cause update to be found`() {
		setupFetchResult(createBrokenBucketFetchResult())
		setupCallbackWithEdition(MAX_VALUE, true)
		verifyAFreenetUriIsFetched()
		verifyNoUpdateFoundEventIsFired()
	}

	private fun createBrokenBucketFetchResult(): FetchResult {
		val clientMetadata = ClientMetadata("text/plain")
		val fetched = object : ArrayBucket("Some other data.".toByteArray()) {
			override fun getInputStream() =
					whenever(mock<InputStream>().read()).thenThrow(IOException()).getMock<InputStream>()
		}
		return FetchResult(clientMetadata, fetched)
	}

	@Test
	fun `invalid time does not cause an update to be found`() {
		setupFetchResult(createInvalidTimeFetchResult())
		setupCallbackWithEdition(MAX_VALUE, true)
		verifyAFreenetUriIsFetched()
		verifyNoUpdateFoundEventIsFired()
	}

	private fun createInvalidTimeFetchResult(): FetchResult {
		val clientMetadata = ClientMetadata("application/xml")
		val fetched = ArrayBucket(("# MapConfigurationBackendVersion=1\n" +
				"CurrentVersion/Version: 0.2\n" +
				"CurrentVersion/ReleaseTime: invalid").toByteArray())
		return FetchResult(clientMetadata, fetched)
	}

	@Test
	fun `invalid properties does not cause an update to be found`() {
		setupFetchResult(createMissingTimeFetchResult())
		setupCallbackWithEdition(MAX_VALUE, true)
		verifyAFreenetUriIsFetched()
		verifyNoUpdateFoundEventIsFired()
	}

	private fun createMissingTimeFetchResult(): FetchResult {
		val clientMetadata = ClientMetadata("application/xml")
		val fetched = ArrayBucket(("# MapConfigurationBackendVersion=1\nCurrentVersion/Version: 0.2\n").toByteArray())
		return FetchResult(clientMetadata, fetched)
	}

	@Test
	fun `invalid version does not cause an update to be found`() {
		setupFetchResult(createInvalidVersionFetchResult())
		setupCallbackWithEdition(MAX_VALUE, true)
		verifyAFreenetUriIsFetched()
		verifyNoUpdateFoundEventIsFired()
	}

	private fun createInvalidVersionFetchResult(): FetchResult {
		val clientMetadata = ClientMetadata("application/xml")
		val fetched = ArrayBucket(("# MapConfigurationBackendVersion=1\n" +
				"CurrentVersion/Version: foo\n" +
				"CurrentVersion/ReleaseTime: 1289417883000").toByteArray())
		return FetchResult(clientMetadata, fetched)
	}

	@Test
	fun `disruptive version gets notification`() {
		setupFetchResult(createDisruptiveVersionFetchResult())
		setupCallbackWithEdition(MAX_VALUE, true)
		verifyAFreenetUriIsFetched()
		verifyEventIsFired(Version(1, 2, 3), 1289417883000L, true)
		verifyThatUpdateCheckerKnowsLatestVersion(Version(1, 2, 3), 1289417883000L)
	}

	private fun createDisruptiveVersionFetchResult(): FetchResult {
		val clientMetadata = ClientMetadata("application/xml")
		val fetched = ArrayBucket(("# MapConfigurationBackendVersion=1\n" +
				"CurrentVersion/Version: 1.2.3\n" +
				"CurrentVersion/ReleaseTime: 1289417883000\n" +
				"DisruptiveVersion/1.2.3: true").toByteArray())
		return FetchResult(clientMetadata, fetched)
	}

}
