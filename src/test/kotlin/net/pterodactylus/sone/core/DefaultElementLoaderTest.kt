package net.pterodactylus.sone.core

import com.google.common.base.Ticker
import freenet.keys.FreenetURI
import net.pterodactylus.sone.core.FreenetInterface.BackgroundFetchCallback
import net.pterodactylus.sone.test.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.TypeSafeDiagnosingMatcher
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.min

/**
 * Unit test for [DefaultElementLoaderTest].
 */
class DefaultElementLoaderTest {

	@Test
	fun `image loader starts request for link that is not known`() {
		runWithCallback(IMAGE_ID) { _, _, _, fetchedUris ->
			assertThat(fetchedUris, contains(freenetURI))
		}
	}

	@Test
	fun `element loader only starts request once`() {
		runWithCallback(IMAGE_ID) { elementLoader, _, _, fetchedUris ->
			elementLoader.loadElement(IMAGE_ID)
			assertThat(fetchedUris, contains(freenetURI))
		}
	}

	@Test
	fun `element loader returns loading element on first call`() {
		runWithCallback(IMAGE_ID) { _, linkedElement, _, _ ->
			assertThat(linkedElement.loading, equalTo(true))
		}
	}

	@Test
	fun `element loader does not cancel on image mime type with 2 mib size`() {
		runWithCallback(IMAGE_ID) { _, _, callback, _ ->
			assertThat(callback.shouldCancel(freenetURI, "image/png", sizeOkay), equalTo(false))
		}
	}

	@Test
	fun `element loader does cancel on image mime type with more than 2 mib size`() {
		runWithCallback(IMAGE_ID) { _, _, callback, _ ->
			assertThat(callback.shouldCancel(freenetURI, "image/png", sizeNotOkay), equalTo(true))
		}
	}

	@Test
	fun `element loader does cancel on audio mime type`() {
		runWithCallback(IMAGE_ID) { _, _, callback, _ ->
			assertThat(callback.shouldCancel(freenetURI, "audio/mpeg", sizeOkay), equalTo(true))
		}
	}

	@Test
	fun `element loader does cancel on video mime type`() {
		runWithCallback(IMAGE_ID) { _, _, callback, _ ->
			assertThat(callback.shouldCancel(freenetURI, "video/mkv", sizeOkay), equalTo(true))
		}
	}

	@Test
	fun `element loader does cancel on text mime type`() {
		runWithCallback(IMAGE_ID) { _, _, callback, _ ->
			assertThat(callback.shouldCancel(freenetURI, "text/plain", sizeOkay), equalTo(true))
		}
	}

	@Test
	fun `element loader does not cancel on text html mime type`() {
		runWithCallback(IMAGE_ID) { _, _, callback, _ ->
			assertThat(callback.shouldCancel(freenetURI, "text/html", sizeOkay), equalTo(false))
		}
	}

	@Test
	fun `image loader can load image`() {
		runWithCallback(decomposedKey) { elementLoader, _, callback, _ ->
			callback.loaded(FreenetURI(normalizedKey), "image/png", read("/static/images/unknown-image-0.png"))
			val linkedElement = elementLoader.loadElement(decomposedKey)
			assertThat(linkedElement, isLinkedElement(equalTo(normalizedKey), allOf(
				hasEntry("type", "image"), hasEntry("size", 2451), hasEntry("sizeHuman", "2 KiB"),
			)))
		}
	}

	@Test
	fun `element loader can extract description from description header`() {
		runWithCallback(textKey) { elementLoader, _, callback, _ ->
			callback.loaded(FreenetURI(textKey), "text/html; charset=UTF-8", read("element-loader.html"))
			val linkedElement = elementLoader.loadElement(textKey)
			assertThat(linkedElement, isLinkedElement(equalTo(textKey), allOf(
				hasEntry("type", "html"), hasEntry("size", 266), hasEntry("sizeHuman", "266 B"),
				hasEntry("title", "Some Nice Page Title"),
				hasEntry("description", "This is an example of a very nice freesite.")
			)))
		}
	}

	@Test
	fun `element loader can extract description from first non-heading paragraph`() {
		runWithCallback(textKey) { elementLoader, _, callback, _ ->
			callback.loaded(FreenetURI(textKey), "text/html; charset=UTF-8", read("element-loader2.html"))
			val linkedElement = elementLoader.loadElement(textKey)
			assertThat(linkedElement, isLinkedElement(equalTo(textKey), allOf(
				hasEntry("type", "html"), hasEntry("size", 185), hasEntry("sizeHuman", "185 B"),
				hasEntry("title", "Some Nice Page Title"),
				hasEntry("description", "This is the first paragraph of the very nice freesite.")
			)))
		}
	}

	@Test
	fun `element loader can extract description if html is more complicated`() {
		runWithCallback(textKey) { elementLoader, _, callback, _ ->
			callback.loaded(FreenetURI(textKey), "text/html; charset=UTF-8", read("element-loader3.html"))
			val linkedElement = elementLoader.loadElement(textKey)
			assertThat(linkedElement, isLinkedElement(equalTo(textKey), allOf(
				hasEntry("type", "html"), hasEntry("size", 204), hasEntry("sizeHuman", "204 B"),
				hasEntry("title", "Some Nice Page Title"),
				hasEntry("description", "This is the first paragraph of the very nice freesite.")
			)))
		}
	}

	@Test
	fun `element loader can not extract title if it is missing`() {
		runWithCallback(textKey) { elementLoader, _, callback, _ ->
			callback.loaded(FreenetURI(textKey), "text/html; charset=UTF-8", read("element-loader4.html"))
			val linkedElement = elementLoader.loadElement(textKey)
			assertThat(linkedElement, isLinkedElement(equalTo(textKey), allOf(
				hasEntry("type", "html"), hasEntry("size", 229), hasEntry("sizeHuman", "229 B"), hasEntry("title", null),
				hasEntry("description", "This is an example of a very nice freesite.")
			)))
		}
	}

	@Test
	fun `image is not loaded again after it failed`() {
		runWithCallback(IMAGE_ID) { elementLoader, _, callback, _ ->
			elementLoader.loadElement(IMAGE_ID)
			callback.failed(freenetURI)
			assertThat(elementLoader.loadElement(IMAGE_ID).failed, equalTo(true))
		}
	}

	@Test
	fun `image is loaded again after failure cache is expired`() {
		runWithCallback(IMAGE_ID, createTicker(1, MINUTES.toNanos(31))) { elementLoader, _, callback, _ ->
			elementLoader.loadElement(IMAGE_ID)
			callback.failed(freenetURI)
			val linkedElement = elementLoader.loadElement(IMAGE_ID)
			assertThat(linkedElement.failed, equalTo(false))
			assertThat(linkedElement.loading, equalTo(true))
		}
	}

	private fun read(resource: String): ByteArray =
		javaClass.getResourceAsStream(resource)?.use { input ->
			ByteArrayOutputStream().use {
				input.copyTo(it)
				it
			}.toByteArray()
		} ?: ByteArray(0)

	@get:Rule
	val silencedLoggin = silencedLogging()

}

private fun runWithCallback(requestUri: String, ticker: Ticker = createTicker(), callbackAction: (elementLoader: ElementLoader, linkedElement: LinkedElement, callback: BackgroundFetchCallback, fetchedUris: List<FreenetURI>) -> Unit) {
	val fetchedUris = mutableListOf<FreenetURI>()
	val callback = AtomicReference<BackgroundFetchCallback>()
	val freenetInterface = overrideStartFetch { uri, backgroundFetchCallback ->
		fetchedUris += uri
		callback.set(backgroundFetchCallback)
	}
	val elementLoader = DefaultElementLoader(freenetInterface, ticker)
	val linkedElement = elementLoader.loadElement(requestUri)
	callbackAction(elementLoader, linkedElement, callback.get(), fetchedUris)
}

private fun overrideStartFetch(action: (FreenetURI, BackgroundFetchCallback) -> Unit) = object : FreenetInterface(null, null, null, null, null, dummyHighLevelSimpleClientCreator) {
	override fun startFetch(uri: FreenetURI, backgroundFetchCallback: BackgroundFetchCallback) {
		action(uri, backgroundFetchCallback)
	}
}

private fun createTicker(vararg times: Long = LongArray(1) { 1 }) = object : Ticker() {
	private var counter = 0
	override fun read() =
		times[min(times.size - 1, counter)]
			.also { counter++ }
}

private fun isLinkedElement(link: Matcher<String> = everything(), properties: Matcher<Map<String, Any?>> = everything(), failed: Matcher<Boolean> = everything(), loading: Matcher<Boolean> = everything()) = object : TypeSafeDiagnosingMatcher<LinkedElement>() {
	override fun matchesSafely(item: LinkedElement, mismatchDescription: Description) =
		handleMatcher(link, item.link, mismatchDescription) &&
				handleMatcher(properties, item.properties, mismatchDescription) &&
				handleMatcher(failed, item.failed, mismatchDescription) &&
				handleMatcher(loading, item.loading, mismatchDescription)

	override fun describeTo(description: Description) {
		description.appendText("is linked element for key matching ").appendValue(link)
			.appendText(", properties matching ").appendValue(properties)
			.appendText(", failed matching ").appendValue(failed)
			.appendText(", loading matching ").appendValue(loading)
	}
}

private const val IMAGE_ID = "KSK@gpl.png"
private val freenetURI = FreenetURI(IMAGE_ID)
private const val decomposedKey = "CHK@DCiVgTWW9nnWHJc9EVwtFJ6jAfBSVyy~rgiPvhUKbS4,mNY85V0x7dYcv7SnEYo1PCC6y2wNWMDNt-y9UWQx9fI,AAMC--8/fru%CC%88hstu%CC%88ck.jpg"
private const val normalizedKey = "CHK@DCiVgTWW9nnWHJc9EVwtFJ6jAfBSVyy~rgiPvhUKbS4,mNY85V0x7dYcv7SnEYo1PCC6y2wNWMDNt-y9UWQx9fI,AAMC--8/frühstück.jpg"
private const val textKey = "KSK@gpl.html"
private const val sizeOkay = 2097152L
private const val sizeNotOkay = sizeOkay + 1
