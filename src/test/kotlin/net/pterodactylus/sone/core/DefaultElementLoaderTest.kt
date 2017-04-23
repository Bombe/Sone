package net.pterodactylus.sone.core

import com.google.common.base.Ticker
import com.google.common.io.ByteStreams
import freenet.keys.FreenetURI
import net.pterodactylus.sone.core.FreenetInterface.BackgroundFetchCallback
import net.pterodactylus.sone.test.capture
import net.pterodactylus.sone.test.mock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Unit test for [DefaultElementLoaderTest].
 */
class DefaultElementLoaderTest {

	companion object {
		private const val IMAGE_ID = "KSK@gpl.png"
		private val freenetURI = FreenetURI(IMAGE_ID)
		private const val decomposedKey = "CHK@DCiVgTWW9nnWHJc9EVwtFJ6jAfBSVyy~rgiPvhUKbS4,mNY85V0x7dYcv7SnEYo1PCC6y2wNWMDNt-y9UWQx9fI,AAMC--8/fru%CC%88hstu%CC%88ck.jpg"
		private const val normalizedKey = "CHK@DCiVgTWW9nnWHJc9EVwtFJ6jAfBSVyy~rgiPvhUKbS4,mNY85V0x7dYcv7SnEYo1PCC6y2wNWMDNt-y9UWQx9fI,AAMC--8/frühstück.jpg"
		private const val textKey = "KSK@gpl.html"
		private val sizeOkay = 2097152L
		private val sizeNotOkay = sizeOkay + 1
	}

	private val freenetInterface = mock<FreenetInterface>()
	private val ticker = mock<Ticker>()
	private val elementLoader = DefaultElementLoader(freenetInterface, ticker)
	private val callback = capture<BackgroundFetchCallback>()

	@Test
	fun `image loader starts request for link that is not known`() {
		elementLoader.loadElement(IMAGE_ID)
		verify(freenetInterface).startFetch(eq(freenetURI), any<BackgroundFetchCallback>())
	}

	@Test
	fun `element loader only starts request once`() {
		elementLoader.loadElement(IMAGE_ID)
		elementLoader.loadElement(IMAGE_ID)
		verify(freenetInterface).startFetch(eq(freenetURI), any<BackgroundFetchCallback>())
	}

	@Test
	fun `element loader returns loading element on first call`() {
		assertThat(elementLoader.loadElement(IMAGE_ID).loading, `is`(true))
	}

	@Test
	fun `element loader does not cancel on image mime type with 2 mib size`() {
		elementLoader.loadElement(IMAGE_ID)
		verify(freenetInterface).startFetch(eq(freenetURI), callback.capture())
		assertThat(callback.value.shouldCancel(freenetURI, "image/png", sizeOkay), `is`(false))
	}

	@Test
	fun `element loader does cancel on image mime type with more than 2 mib size`() {
		elementLoader.loadElement(IMAGE_ID)
		verify(freenetInterface).startFetch(eq(freenetURI), callback.capture())
		assertThat(callback.value.shouldCancel(freenetURI, "image/png", sizeNotOkay), `is`(true))
	}

	@Test
	fun `element loader does cancel on audio mime type`() {
		elementLoader.loadElement(IMAGE_ID)
		verify(freenetInterface).startFetch(eq(freenetURI), callback.capture())
		assertThat(callback.value.shouldCancel(freenetURI, "audio/mpeg", sizeOkay), `is`(true))
	}

	@Test
	fun `element loader does cancel on video mime type`() {
		elementLoader.loadElement(IMAGE_ID)
		verify(freenetInterface).startFetch(eq(freenetURI), callback.capture())
		assertThat(callback.value.shouldCancel(freenetURI, "video/mkv", sizeOkay), `is`(true))
	}

	@Test
	fun `element loader does cancel on text mime type`() {
		elementLoader.loadElement(IMAGE_ID)
		verify(freenetInterface).startFetch(eq(freenetURI), callback.capture())
		assertThat(callback.value.shouldCancel(freenetURI, "text/plain", sizeOkay), `is`(true))
	}

	@Test
	fun `element loader does not cancel on text html mime type`() {
		elementLoader.loadElement(IMAGE_ID)
		verify(freenetInterface).startFetch(eq(freenetURI), callback.capture())
		assertThat(callback.value.shouldCancel(freenetURI, "text/html", sizeOkay), `is`(false))
	}

	@Test
	fun `image loader can load image`() {
		elementLoader.loadElement(decomposedKey)
		verify(freenetInterface).startFetch(eq(FreenetURI(decomposedKey)), callback.capture())
		callback.value.loaded(FreenetURI(normalizedKey), "image/png", read("/static/images/unknown-image-0.png"))
		val linkedElement = elementLoader.loadElement(decomposedKey)
		assertThat(linkedElement, `is`(LinkedElement(normalizedKey, properties = mapOf(
				"type" to "image", "size" to 2451, "sizeHuman" to "2 KiB"
		))))
	}

	@Test
	fun `element loader can extract description from description header`() {
	    elementLoader.loadElement(textKey)
		verify(freenetInterface).startFetch(eq(FreenetURI(textKey)), callback.capture())
		callback.value.loaded(FreenetURI(textKey), "text/html; charset=UTF-8", read("element-loader.html"))
		val linkedElement = elementLoader.loadElement(textKey)
		assertThat(linkedElement, equalTo(LinkedElement(textKey, properties = mapOf(
				"type" to "html",
				"size" to 266,
				"sizeHuman" to "266 B",
				"title" to "Some Nice Page Title",
				"description" to "This is an example of a very nice freesite."
		))))
	}

	@Test
	fun `element loader can extract description from first non-heading paragraph`() {
	    elementLoader.loadElement(textKey)
		verify(freenetInterface).startFetch(eq(FreenetURI(textKey)), callback.capture())
		callback.value.loaded(FreenetURI(textKey), "text/html; charset=UTF-8", read("element-loader2.html"))
		val linkedElement = elementLoader.loadElement(textKey)
		assertThat(linkedElement, equalTo(LinkedElement(textKey, properties = mapOf(
				"type" to "html",
				"size" to 185,
				"sizeHuman" to "185 B",
				"title" to "Some Nice Page Title",
				"description" to "This is the first paragraph of the very nice freesite."
		))))
	}

	@Test
	fun `element loader can not extract description if html is more complicated`() {
	    elementLoader.loadElement(textKey)
		verify(freenetInterface).startFetch(eq(FreenetURI(textKey)), callback.capture())
		callback.value.loaded(FreenetURI(textKey), "text/html; charset=UTF-8", read("element-loader3.html"))
		val linkedElement = elementLoader.loadElement(textKey)
		assertThat(linkedElement, equalTo(LinkedElement(textKey, properties = mapOf(
				"type" to "html",
				"size" to 204,
				"sizeHuman" to "204 B",
				"title" to "Some Nice Page Title",
				"description" to null
		))))
	}

	@Test
	fun `element loader can not extract title if it is missing`() {
	    elementLoader.loadElement(textKey)
		verify(freenetInterface).startFetch(eq(FreenetURI(textKey)), callback.capture())
		callback.value.loaded(FreenetURI(textKey), "text/html; charset=UTF-8", read("element-loader4.html"))
		val linkedElement = elementLoader.loadElement(textKey)
		assertThat(linkedElement, equalTo(LinkedElement(textKey, properties = mapOf(
				"type" to "html",
				"size" to 229,
				"sizeHuman" to "229 B",
				"title" to null,
				"description" to "This is an example of a very nice freesite."
		))))
	}

	@Test
	fun `image is not loaded again after it failed`() {
		elementLoader.loadElement(IMAGE_ID)
		verify(freenetInterface).startFetch(eq(freenetURI), callback.capture())
		callback.value.failed(freenetURI)
		assertThat(elementLoader.loadElement(IMAGE_ID).failed, `is`(true))
		verify(freenetInterface).startFetch(eq(freenetURI), callback.capture())
	}

	@Test
	fun `image is loaded again after failure cache is expired`() {
		elementLoader.loadElement(IMAGE_ID)
		verify(freenetInterface).startFetch(eq(freenetURI), callback.capture())
		callback.value.failed(freenetURI)
		`when`(ticker.read()).thenReturn(TimeUnit.MINUTES.toNanos(31))
		val linkedElement = elementLoader.loadElement(IMAGE_ID)
		assertThat(linkedElement.failed, `is`(false))
		assertThat(linkedElement.loading, `is`(true))
		verify(freenetInterface, times(2)).startFetch(eq(freenetURI), callback.capture())
	}

	private fun read(resource: String): ByteArray =
			javaClass.getResourceAsStream(resource)?.use { input ->
				ByteArrayOutputStream().use {
					ByteStreams.copy(input, it)
					it
				}.toByteArray()
			} ?: ByteArray(0)

}
