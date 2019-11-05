package net.pterodactylus.sone.core

import freenet.keys.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.equalTo
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import kotlin.test.Test

/**
 * Unit test for [SoneRescuer].
 */
class SoneRescuerTest {

	private val core = mock<Core>()
	private val soneDownloader = mock<SoneDownloader>()
	private val sone = mock<Sone>().apply {
		val soneUri = mock<FreenetURI>()
		whenever(soneUri.edition).thenReturn(currentEdition)
		whenever(requestUri).thenReturn(soneUri)
	}
	private val soneRescuer = SoneRescuer(core, soneDownloader, sone)

	@Test
	fun newSoneRescuerIsNotFetchingAnything() {
		assertThat(soneRescuer.isFetching, equalTo(false))
	}

	@Test
	fun newSoneRescuerStartsAtCurrentEditionOfSone() {
		assertThat(soneRescuer.currentEdition, equalTo(currentEdition))
	}

	@Test
	fun newSoneRescuerHasANextEditionToGet() {
		assertThat(soneRescuer.hasNextEdition(), equalTo(true))
	}

	@Test
	fun soneRescuerDoesNotHaveANextEditionIfCurrentEditionIsZero() {
		whenever(sone.requestUri.edition).thenReturn(0L)
		val soneRescuer = SoneRescuer(core, soneDownloader, sone)
		assertThat(soneRescuer.hasNextEdition(), equalTo(false))
	}

	@Test
	fun nextEditionIsOneSmallerThanTheCurrentEdition() {
		assertThat(soneRescuer.nextEdition, equalTo(currentEdition - 1))
	}

	@Test
	fun lastFetchOfANewSoneRescuerWasSuccessful() {
		assertThat(soneRescuer.isLastFetchSuccessful, equalTo(true))
	}

	@Test
	fun mainLoopStopsWhenItShould() {
		soneRescuer.stop()
		soneRescuer.serviceRun()
	}

	@Test
	fun successfulInsert() {
		val fetchedSone = mock<Sone>()
		returnUriOnInsert(fetchedSone, currentEdition - 1)
		soneRescuer.startNextFetch()
		soneRescuer.serviceRun()
		verify(core).lockSone(eq(sone))
		verify(core).updateSone(eq(fetchedSone), eq(true))
		assertThat(soneRescuer.isLastFetchSuccessful, equalTo(true))
		assertThat(soneRescuer.isFetching, equalTo(false))
		assertThat(soneRescuer.currentEdition, equalTo(currentEdition - 1))
	}

	@Test
	fun `starting fetch with skipping one edition skips one edition`() {
		val fetchedSone = mock<Sone>()
		returnUriOnInsert(fetchedSone, currentEdition - 2)
		soneRescuer.startNextFetchWithSkip()
		soneRescuer.serviceRun()
		verify(core).lockSone(eq(sone))
		verify(core).updateSone(eq(fetchedSone), eq(true))
		assertThat(soneRescuer.isLastFetchSuccessful, equalTo(true))
		assertThat(soneRescuer.isFetching, equalTo(false))
		assertThat(soneRescuer.currentEdition, equalTo(currentEdition - 2))
	}

	@Test
	fun nonSuccessfulInsertIsRecognized() {
		returnUriOnInsert(null, (currentEdition - 1))
		soneRescuer.startNextFetch()
		soneRescuer.serviceRun()
		verify(core).lockSone(eq(sone))
		verify(core, never()).updateSone(any(Sone::class.java), eq(true))
		assertThat(soneRescuer.isLastFetchSuccessful, equalTo(false))
		assertThat(soneRescuer.isFetching, equalTo(false))
		assertThat(soneRescuer.currentEdition, equalTo(currentEdition))
	}

	private fun returnUriOnInsert(fetchedSone: Sone?, edition: Long) {
		val keyWithMetaStrings = setupFreenetUri(edition)
		doAnswer {
			soneRescuer.stop()
			fetchedSone
		}.whenever(soneDownloader).fetchSone(eq(sone), eq(keyWithMetaStrings), eq(true))
	}

	private fun setupFreenetUri(edition: Long): FreenetURI {
		val sskKey = mock<FreenetURI>()
		val keyWithDocName = mock<FreenetURI>()
		val keyWithMetaStrings = mock<FreenetURI>()
		whenever(keyWithDocName.setMetaString(eq(arrayOf("sone.xml")))).thenReturn(keyWithMetaStrings)
		whenever(sskKey.setDocName(eq("Sone-" + edition))).thenReturn(keyWithDocName)
		whenever(sone.requestUri.setKeyType(eq("SSK"))).thenReturn(sskKey)
		return keyWithMetaStrings
	}

}

private const val currentEdition = 12L
