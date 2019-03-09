package net.pterodactylus.sone.template

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.database.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.text.*
import net.pterodactylus.sone.text.Part
import net.pterodactylus.util.template.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.jsoup.*
import org.jsoup.nodes.*
import org.junit.*
import org.mockito.*
import java.net.*

/**
 * Unit test for [RenderFilter].
 */
class RenderFilterTest {

	companion object {
		private const val FREEMAIL_ID = "t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra"
		private const val SONE_FREEMAIL = "sone@$FREEMAIL_ID.freemail"
		private const val SONE_IDENTITY = "nwa8lHa271k2QvJ8aa0Ov7IHAV-DFOCFgmDt3X6BpCI"
		private const val POST_ID = "37a06250-6775-4b94-86ff-257ba690953c"
	}

	private val soneProvider = mock<SoneProvider>()
	private val soneTextParser = mock<SoneTextParser>()
	private val htmlFilter = HtmlFilter()
	private val sone = setupSone(SONE_IDENTITY, "Sone", "First")
	private val parameters = mutableMapOf<String, Any?>()

	private val filter = RenderFilter(soneProvider, soneTextParser, htmlFilter)
	private val templateContext = TemplateContext()

	@Test
	fun `plain text part is rendered correctly`() {
		assertThat(renderParts(PlainTextPart("plain text")), equalTo("plain text"))
	}

	private fun renderParts(vararg part: Part) = filter.format(templateContext, listOf(*part), parameters) as String

	@Test
	fun `freenet link is rendered correctly`() {
		val linkNode = renderParts(FreenetLinkPart("KSK@gpl.txt", "gpl.txt", false)).toLinkNode()
		verifyLink(linkNode, "/KSK@gpl.txt", "freenet", "KSK@gpl.txt", "gpl.txt")
	}

	private fun verifyLink(linkNode: Element, url: String, cssClass: String, tooltip: String, text: String) {
		assertThat(linkNode.nodeName(), equalTo("a"))
		assertThat<List<Attribute>>(linkNode.attributes().asList(), containsInAnyOrder(
				Attribute("href", url),
				Attribute("class", cssClass),
				Attribute("title", tooltip)
		))
		assertThat(linkNode.text(), equalTo(text))
	}

	@Test
	fun `trusted freenet link is rendered with correct css class`() {
		val linkNode = renderParts(FreenetLinkPart("KSK@gpl.txt", "gpl.txt", true)).toLinkNode()
		verifyLink(linkNode, "/KSK@gpl.txt", "freenet-trusted", "KSK@gpl.txt", "gpl.txt")
	}

	private fun String.toLinkNode() = Jsoup.parseBodyFragment(this).body().child(0)

	@Test
	fun `internet link is rendered correctly`() {
		val linkNode = renderParts(LinkPart("http://test.com/test.html", "test.com/test.html")).toLinkNode()
		verifyLink(linkNode, "/external-link/?_CHECKED_HTTP_=${URLEncoder.encode("http://test.com/test.html", "UTF-8")}", "internet",
				"http://test.com/test.html", "test.com/test.html")
	}

	@Test
	fun `sone parts are rendered correctly`() {
		val linkNode = renderParts(SonePart(sone)).toLinkNode()
		verifyLink(linkNode, "viewSone.html?sone=" + SONE_IDENTITY, "in-sone", "First", "First")
	}

	private fun setupSone(identity: String, name: String?, firstName: String): Sone {
		val sone = mock<Sone>()
		whenever(sone.id).thenReturn(identity)
		whenever(sone.profile).thenReturn(Profile(sone))
		whenever(sone.name).thenReturn(name)
		sone.profile.firstName = firstName
		whenever(soneProvider.getSone(identity)).thenReturn(sone)
		return sone
	}

	@Test
	fun `sone part with unknown sone is rendered as link to web of trust`() {
		val sone = setupSone(SONE_IDENTITY, null, "First")
		val linkNode = renderParts(SonePart(sone)).toLinkNode()
		verifyLink(linkNode, "/WebOfTrust/ShowIdentity?id=$SONE_IDENTITY", "in-sone", SONE_IDENTITY, SONE_IDENTITY)
	}

	@Test
	fun `post part is cut off correctly when there are spaces`() {
		val post = setupPost(sone, "1234 678901 345 789012 45678 01.")
		whenever(soneTextParser.parse(eq("1234 678901 345 789012 45678 01."), ArgumentMatchers.any()))
				.thenReturn(listOf(PlainTextPart("1234 678901 345 789012 45678 01.")))
		val linkNode = renderParts(PostPart(post)).toLinkNode()
		verifyLink(linkNode, "viewPost.html?post=$POST_ID", "in-sone", "First", "1234 678901 345…")
	}

	private fun setupPost(sone: Sone, value: String) =
			mock<Post>().apply {
				whenever(id).thenReturn(POST_ID)
				whenever(this.sone).thenReturn(this@RenderFilterTest.sone)
				whenever(text).thenReturn(value)
			}

	@Test
	fun `post part is cut off correctly when there are no spaces`() {
		val post = setupPost(sone, "1234567890123456789012345678901.")
		whenever(soneTextParser.parse(eq("1234567890123456789012345678901."), ArgumentMatchers.any()))
				.thenReturn(listOf(PlainTextPart("1234567890123456789012345678901.")))
		val linkNode = renderParts(PostPart(post)).toLinkNode()
		verifyLink(linkNode, "viewPost.html?post=$POST_ID", "in-sone", "First", "12345678901234567890…")
	}

	@Test
	fun `post part shorter than 21 chars is not cut off`() {
		val post = setupPost(sone, "12345678901234567890")
		whenever(soneTextParser.parse(eq("12345678901234567890"), ArgumentMatchers.any()))
				.thenReturn(listOf(PlainTextPart("12345678901234567890")))
		val linkNode = renderParts(PostPart(post)).toLinkNode()
		verifyLink(linkNode, "viewPost.html?post=$POST_ID", "in-sone", "First", "12345678901234567890")
	}

	@Test
	fun `multiple parts are rendered correctly`() {
		val parts = arrayOf(PlainTextPart("te"), PlainTextPart("xt"))
		assertThat(renderParts(*parts), equalTo("text"))
	}

	@Test
	fun `freemail address is displayed correctly`() {
		val linkNode = renderParts(FreemailPart("sone", FREEMAIL_ID, SONE_IDENTITY)).toLinkNode()
		verifyLink(linkNode, "/Freemail/NewMessage?to=$SONE_IDENTITY", "in-sone", "First\n$SONE_FREEMAIL", "sone@First.freemail")
	}

}
