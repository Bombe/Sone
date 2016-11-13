package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.ElementLoader
import net.pterodactylus.sone.core.LinkedElement
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.text.FreenetLinkPart
import net.pterodactylus.sone.text.LinkPart
import net.pterodactylus.sone.text.PlainTextPart
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Test
import org.mockito.Mockito.`when`

/**
 * Unit test for [LinkedElementsFilter].
 */
class LinkedElementsFilterTest {

	private val imageLoader = mock<ElementLoader>()
	private val filter = LinkedElementsFilter(imageLoader)

	@Test
	fun `filter finds all loaded freenet images`() {
		val parts = listOf(
				PlainTextPart("text"),
				LinkPart("http://link", "link"),
				FreenetLinkPart("KSK@link", "link", false),
				FreenetLinkPart("KSK@loading.png", "link", false),
				FreenetLinkPart("KSK@link.png", "link", false)
		)
		`when`(imageLoader.loadElement("KSK@link")).thenReturn(LinkedElement("KSK@link", failed = true))
		`when`(imageLoader.loadElement("KSK@loading.png")).thenReturn(LinkedElement("KSK@loading.png", loading = true))
		`when`(imageLoader.loadElement("KSK@link.png")).thenReturn(LinkedElement("KSK@link.png"))
		val loadedImages = filter.format(null, parts, null)
		assertThat(loadedImages, contains<LinkedElement>(
				LinkedElement("KSK@loading.png", failed = false, loading = true),
				LinkedElement("KSK@link.png", failed = false, loading = false)
		))
	}

}
