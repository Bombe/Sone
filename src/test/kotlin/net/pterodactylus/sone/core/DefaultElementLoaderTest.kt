package net.pterodactylus.sone.core

import com.google.common.io.ByteStreams
import com.google.common.io.Files
import freenet.keys.FreenetURI
import net.pterodactylus.sone.core.FreenetInterface.BackgroundFetchCallback
import net.pterodactylus.sone.test.capture
import net.pterodactylus.sone.test.mock
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.io.ByteArrayOutputStream

/**
 * Unit test for [DefaultElementLoaderTest].
 */
class DefaultElementLoaderTest {

	companion object {
		private const val IMAGE_ID = "KSK@gpl.png"
	}

	private val freenetInterface = mock<FreenetInterface>()
	private val elementLoader = DefaultElementLoader(freenetInterface)
	private val callback = capture<BackgroundFetchCallback>()

	@Test
	fun `image loader starts request for link that is not known`() {
		elementLoader.loadElement(IMAGE_ID)
		verify(freenetInterface).startFetch(eq(FreenetURI(IMAGE_ID)), any<BackgroundFetchCallback>())
	}

	@Test
	fun `element loader only starts request once`() {
		elementLoader.loadElement(IMAGE_ID)
		elementLoader.loadElement(IMAGE_ID)
		verify(freenetInterface).startFetch(eq(FreenetURI(IMAGE_ID)), any<BackgroundFetchCallback>())
	}

	@Test
	fun `element loader returns loading element on first call`() {
		assertThat(elementLoader.loadElement(IMAGE_ID).loading, `is`(true))
	}

	@Test
	fun `image loader can load image`() {
		elementLoader.loadElement(IMAGE_ID)
		verify(freenetInterface).startFetch(eq(FreenetURI(IMAGE_ID)), callback.capture())
	    callback.value.loaded(FreenetURI(IMAGE_ID), "image/png", read("/static/images/unknown-image-0.png"))
		val linkedElement = elementLoader.loadElement(IMAGE_ID)
		assertThat(linkedElement.link, `is`(IMAGE_ID))
		assertThat(linkedElement.loading, `is`(false))
		assertThat(linkedElement, instanceOf(LinkedImage::class.java))
	}

	@Test
	fun `image can be loaded again after it failed`() {
		elementLoader.loadElement(IMAGE_ID)
		verify(freenetInterface).startFetch(eq(FreenetURI(IMAGE_ID)), callback.capture())
		callback.value.failed(FreenetURI(IMAGE_ID))
		elementLoader.loadElement(IMAGE_ID)
		verify(freenetInterface, times(2)).startFetch(eq(FreenetURI(IMAGE_ID)), callback.capture())
	}

	private fun read(resource: String): ByteArray =
			javaClass.getResourceAsStream(resource)?.use { input ->
				ByteArrayOutputStream().use {
					ByteStreams.copy(input, it)
					it
				}.toByteArray()
			} ?: ByteArray(0)

}
