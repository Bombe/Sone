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
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.verify
import java.io.ByteArrayOutputStream

/**
 * Unit test for [DefaultImageLoaderTest].
 */
class DefaultImageLoaderTest {

	companion object {
		private const val IMAGE_ID = "KSK@gpl.png"
	}

	private val freenetInterface = mock<FreenetInterface>()
	private val imageLoader = DefaultImageLoader(freenetInterface)
	private val callback = capture<BackgroundFetchCallback>()

	@Test
	fun `image loader starts request for link that is not known`() {
		assertThat(imageLoader.toLoadedImage(IMAGE_ID), nullValue())
		verify(freenetInterface).startFetch(eq(FreenetURI(IMAGE_ID)), any<BackgroundFetchCallback>())
	}

	@Test
	fun `image loader can load image`() {
		assertThat(imageLoader.toLoadedImage(IMAGE_ID), nullValue())
		verify(freenetInterface).startFetch(eq(FreenetURI(IMAGE_ID)), callback.capture())
	    callback.value.loaded(FreenetURI(IMAGE_ID), "image/png", read("/static/images/unknown-image-0.png"))
		val loadedImage = imageLoader.toLoadedImage(IMAGE_ID)!!
		assertThat(loadedImage.link, `is`(IMAGE_ID))
		assertThat(loadedImage.mimeType, `is`("image/png"))
		assertThat(loadedImage.width, `is`(200))
		assertThat(loadedImage.height, `is`(150))
	}

	private fun read(resource: String): ByteArray =
			javaClass.getResourceAsStream(resource)?.use { input ->
				ByteArrayOutputStream().use {
					ByteStreams.copy(input, it)
					it
				}.toByteArray()
			} ?: ByteArray(0)

}
