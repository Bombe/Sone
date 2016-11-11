package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.ImageLoader
import net.pterodactylus.sone.core.LoadedImage
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.text.FreenetLinkPart
import net.pterodactylus.sone.text.LinkPart
import net.pterodactylus.sone.text.PlainTextPart
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test
import org.mockito.Mockito.`when`

/**
 * Unit test for [LinkedImagesFilter].
 */
class LinkedImagesFilterTest {

	private val imageLoader = mock<ImageLoader>()
	private val filter = LinkedImagesFilter(imageLoader)

	@Test
	fun `filter finds all loaded freenet images`() {
		val parts = listOf(
				PlainTextPart("text"),
				LinkPart("http://link", "link"),
				FreenetLinkPart("KSK@link", "link", false),
				FreenetLinkPart("KSK@link.png", "link", false)
		)
		`when`(imageLoader.toLoadedImage("KSK@link.png")).thenReturn(LoadedImage("KSK@link.png", "image/png", 1440, 900))
		val loadedImages = filter.format(null, parts, null)
		assertThat(loadedImages, Matchers.contains(
				LoadedImage("KSK@link.png", "image/png", 1440, 900)
		))
	}

}
