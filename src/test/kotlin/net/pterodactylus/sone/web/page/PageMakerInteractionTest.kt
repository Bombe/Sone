package net.pterodactylus.sone.web.page

import freenet.clients.http.*
import freenet.support.*
import freenet.support.HTMLNode.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.test.TestUtil.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*

@Dirty
class PageMakerInteractionTest {

	private val toadletContext = deepMock<ToadletContext>()
	private val pageMaker: PageMaker = toadletContext.pageMaker
	private val outerNode = HTMLDoctype("html", "-//W3C//DTD XHTML 1.1//EN")
	private val htmlNode: HTMLNode = outerNode.addChild("html")
	private val headNode: HTMLNode = htmlNode.addChild("head")
	private val contentNode: HTMLNode = htmlNode.addChild("body").addChild("div")
	private val pageNode: PageNode = createObject(PageNode::class.java, arrayOf(HTMLNode::class.java, HTMLNode::class.java, HTMLNode::class.java), outerNode, headNode, contentNode)

	init {
		whenever(pageMaker.getPageNode("page title", toadletContext)).thenReturn(pageNode)
	}

	private val pageMakerInteractions = PageMakerInteraction(toadletContext, "page title")

	@Test
	fun `interactions can add style sheet`() {
		pageMakerInteractions.addStyleSheet("style.sheet")
		assertThat(headNode.children.filter { it.name == "link" }.map { it.attributes }, contains(
				mapOf("rel" to "stylesheet", "href" to "style.sheet", "type" to "text/css", "media" to "screen")
		))
	}

	@Test
	fun `link nodes can be added`() {
		pageMakerInteractions.addLinkNode(mapOf("foo" to "bar"))
		assertThat(headNode.children.filter { it.name == "link" }.map { it.attributes }, contains(
				mapOf("foo" to "bar")
		))
	}

	@Test
	fun `shortcut icon can be added`() {
		pageMakerInteractions.addShortcutIcon("shortcut.icon")
		assertThat(headNode.children.filter { it.name == "link" }.map { it.attributes }, contains(
				mapOf("rel" to "icon", "href" to "shortcut.icon")
		))
	}

	@Test
	fun `content can be set`() {
		pageMakerInteractions.setContent("foo<bar")
		assertThat(contentNode.generate(), containsString("foo<bar"))
	}

	@Test
	fun `whole page can be rendered`() {
		pageMakerInteractions.setContent("foo<bar")
		assertThat(pageMakerInteractions.renderPage(), containsString("foo<bar"))
	}

	private val HTMLNode.name: String get() = firstTag

}
