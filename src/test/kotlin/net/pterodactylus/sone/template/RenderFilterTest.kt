package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.text.FreemailPart
import net.pterodactylus.sone.text.FreenetLinkPart
import net.pterodactylus.sone.text.LinkPart
import net.pterodactylus.sone.text.Part
import net.pterodactylus.sone.text.PlainTextPart
import net.pterodactylus.sone.text.PostPart
import net.pterodactylus.sone.text.SonePart
import net.pterodactylus.util.template.HtmlFilter
import net.pterodactylus.util.template.TemplateContext
import net.pterodactylus.util.template.TemplateContextFactory
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.containsInAnyOrder
import org.jsoup.Jsoup
import org.jsoup.nodes.Attribute
import org.jsoup.nodes.Element
import org.junit.Test
import org.mockito.Mockito.`when`
import java.net.URLEncoder

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

	private val core = mock<Core>()
	private val templateContextFactory = TemplateContextFactory()
	private val templateContext: TemplateContext
	private val sone = setupSone(SONE_IDENTITY, "Sone", "First")
	private val parameters = mutableMapOf<String, Any?>()

	init {
		templateContextFactory.addFilter("html", HtmlFilter())
		templateContext = templateContextFactory.createTemplateContext()
	}

	private val filter = RenderFilter(core, templateContextFactory)

	@Test
	fun `plain text part is rendered correctly`() {
		assertThat(renderParts(PlainTextPart("plain text")), `is`("plain text"))
	}

	private fun renderParts(vararg part: Part) = filter.format(templateContext, listOf(*part), parameters) as String

	@Test
	fun `freenet link is rendered correctly`() {
		val linkNode = renderParts(FreenetLinkPart("KSK@gpl.txt", "gpl.txt", false)).toLinkNode()
		verifyLink(linkNode, "/KSK@gpl.txt", "freenet", "KSK@gpl.txt", "gpl.txt")
	}

	private fun verifyLink(linkNode: Element, url: String, cssClass: String, tooltip: String, text: String) {
		assertThat(linkNode.nodeName(), `is`("a"))
		assertThat<List<Attribute>>(linkNode.attributes().asList(), containsInAnyOrder(
				Attribute("href", url),
				Attribute("class", cssClass),
				Attribute("title", tooltip)
		))
		assertThat(linkNode.text(), `is`(text))
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
		`when`(sone.id).thenReturn(identity)
		`when`(sone.profile).thenReturn(Profile(sone))
		`when`(sone.name).thenReturn(name)
		sone.profile.firstName = firstName
		`when`(core.getSone(identity)).thenReturn(sone)
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
		val linkNode = renderParts(PostPart(post)).toLinkNode()
		verifyLink(linkNode, "viewPost.html?post=$POST_ID", "in-sone", "First", "1234 678901 345…")
	}

	private fun setupPost(sone: Sone, value: String): Post {
		val post = mock<Post>()
		`when`(post.id).thenReturn(POST_ID)
		`when`(post.sone).thenReturn(sone)
		`when`(post.text).thenReturn(value)
		return post
	}

	@Test
	fun `post part is cut off correctly when there are no spaces`() {
		val post = setupPost(sone, "1234567890123456789012345678901.")
		val linkNode = renderParts(PostPart(post)).toLinkNode()
		verifyLink(linkNode, "viewPost.html?post=$POST_ID", "in-sone", "First", "12345678901234567890…")
	}

	@Test
	fun `post part shorter than 21 chars is not cut off`() {
		val post = setupPost(sone, "12345678901234567890")
		val linkNode = renderParts(PostPart(post)).toLinkNode()
		verifyLink(linkNode, "viewPost.html?post=$POST_ID", "in-sone", "First", "12345678901234567890")
	}

	@Test
	fun `multiple parts are rendered correctly`() {
		val parts = arrayOf(PlainTextPart("te"), PlainTextPart("xt"))
		assertThat(renderParts(*parts), `is`("text"))
	}

	@Test
	fun `freemail address is displayed correctly`() {
		val linkNode = renderParts(FreemailPart("sone", FREEMAIL_ID, SONE_IDENTITY)).toLinkNode()
		verifyLink(linkNode, "/Freemail/NewMessage?to=$SONE_IDENTITY", "in-sone", "First\n$SONE_FREEMAIL", "sone@First.freemail")
	}

}
