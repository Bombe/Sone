package net.pterodactylus.sone.template

import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.text.FreenetLinkPart
import net.pterodactylus.sone.text.Part
import net.pterodactylus.sone.text.PlainTextPart
import net.pterodactylus.sone.text.SonePart
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Test

/**
 * Unit test for [ShortenFilter].
 */
class ShortenFilterTest {

	private val filter = ShortenFilter()

	@Suppress("UNCHECKED_CAST")
	private fun shortenParts(length: Int, cutOffLength: Int, vararg parts: Part) =
			filter.format(null, listOf(*parts), mapOf("cut-off-length" to cutOffLength, "length" to length)) as Iterable<Part>

	@Test
	fun `plain text part is shortened if length exceeds maxl ength`() {
		assertThat(shortenParts(15, 10, PlainTextPart("This is a long text.")), contains<Part>(
				PlainTextPart("This is a …")
		))
	}

	@Test
	fun `plain text part is not shortened if length does not exceed max length`() {
		assertThat(shortenParts(20, 10, PlainTextPart("This is a long text.")), contains<Part>(
				PlainTextPart("This is a long text.")
		))
	}

	@Test
	fun `short parts are not shortened`() {
		assertThat(shortenParts(15, 10, PlainTextPart("This.")), contains<Part>(
				PlainTextPart("This.")
		))
	}

	@Test
	fun `multiple plain text parts are shortened`() {
		assertThat(shortenParts(15, 10, PlainTextPart("This "), PlainTextPart("is a long text.")), contains<Part>(
				PlainTextPart("This "),
				PlainTextPart("is a …")
		))
	}

	@Test
	fun `parts after length has been reached are ignored`() {
		assertThat(shortenParts(15, 10, PlainTextPart("This is a long text."), PlainTextPart(" And even more.")), contains<Part>(
				PlainTextPart("This is a …")
		))
	}

	@Test
	fun `link parts are not shortened`() {
		assertThat(shortenParts(15, 10, FreenetLinkPart("KSK@gpl.txt", "This is a long text.", false)), contains<Part>(
				FreenetLinkPart("KSK@gpl.txt", "This is a long text.", false)
		))
	}

	@Test
	fun `additional link parts are ignored`() {
		assertThat(shortenParts(15, 10, PlainTextPart("This is a long text."), FreenetLinkPart("KSK@gpl.txt", "This is a long text.", false)), contains<Part>(
				PlainTextPart("This is a …")
		))
	}

	@Test
	fun `sone parts are added but their length is ignored`() {
		val sone = mock<Sone>()
		whenever(sone.profile).thenReturn(Profile(sone))
		assertThat(shortenParts(15, 10, SonePart(sone), PlainTextPart("This is a long text.")), contains<Part>(
				SonePart(sone),
				PlainTextPart("This is a …")
		))
	}

	@Test
	fun `additional sone parts are ignored`() {
		val sone = mock<Sone>()
		whenever(sone.profile).thenReturn(Profile(sone))
		assertThat(shortenParts(15, 10, PlainTextPart("This is a long text."), SonePart(sone)), contains<Part>(
				PlainTextPart("This is a …")
		))
	}

}

