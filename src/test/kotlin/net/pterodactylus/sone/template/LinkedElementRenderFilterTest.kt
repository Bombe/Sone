package net.pterodactylus.sone.template

import com.google.inject.*
import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.util.template.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.jsoup.*
import org.jsoup.nodes.*
import org.junit.*

/**
 * Unit test for [LinkedElementRenderFilter].
 */
class LinkedElementRenderFilterTest {

	private val filter = LinkedElementRenderFilter()

	@Test
	fun `filter returns null for objects that are not linked elements`() {
		assertThat(filter.format(null, Any(), null), nullValue())
	}

	@Test
	fun `filter renders empty span for not loaded elements`() {
		val html = filter.format(null, LinkedElement("KSK@gpl.png", loading = true), emptyMap()) as String
		val spanNode = Jsoup.parseBodyFragment(html).body().child(0)
		assertThat(spanNode.nodeName(), equalTo("span"))
		assertThat(spanNode.attr("class"), equalTo("linked-element not-loaded"))
		assertThat(spanNode.attr("title"), equalTo("KSK@gpl.png"))
		assertThat(spanNode.hasAttr("style"), equalTo(false))
		assertThat(spanNode.children().isEmpty(), equalTo(true))
	}

	@Test
	fun `filter can render linked images`() {
		val html = filter.format(null, LinkedElement("KSK@gpl.png", properties = mapOf("type" to "image")), emptyMap()) as String
		val outerSpanNode = Jsoup.parseBodyFragment(html).body().child(0)
		assertThat(outerSpanNode.nodeName(), equalTo("span"))
		assertThat(outerSpanNode.attr("class"), equalTo("linked-element loaded"))
		assertThat(outerSpanNode.attr("title"), equalTo("KSK@gpl.png"))
		val linkNode = outerSpanNode.child(0)
		assertThat(linkNode.nodeName(), equalTo("a"))
		assertThat(linkNode.attr("href"), equalTo("/KSK@gpl.png"))
		val innerSpanNode = linkNode.child(0)
		assertThat(innerSpanNode.attr("style"), equalTo("background-image: url('/KSK@gpl.png')"))
	}

	@Test
	fun `filter can render HTML pages`() {
		val html = filter.format(null, LinkedElement("KSK@gpl.html", properties = mapOf("type" to "html", "title" to "Page Title", "description" to "This is the description.")), emptyMap()) as String
		val outerSpanNode = Jsoup.parseBodyFragment(html).body().child(0)
		assertThat(outerSpanNode.nodeName(), equalTo("span"))
		assertThat(outerSpanNode.attr("class"), equalTo("linked-element loaded"))
		assertThat(outerSpanNode.attr("title"), equalTo("KSK@gpl.html"))
		val linkNode = outerSpanNode.child(0)
		assertThat(linkNode.nodeName(), equalTo("a"))
		assertThat(linkNode.attr("href"), equalTo("/KSK@gpl.html"))
		val divNodes = linkNode.children()
		assertThat(divNodes.map(Element::nodeName), contains("div", "div"))
		assertThat(divNodes.map { it.attr("class") }, contains("heading", "description"))
		assertThat(divNodes.map(Element::text), contains("Page Title", "This is the description."))
	}

	@Test
	fun `render filter can be created by guice`() {
		val injector = Guice.createInjector(TemplateContextFactory::class.isProvidedByMock())
		assertThat(injector.getInstance<LinkedElementRenderFilter>(), notNullValue())
	}

}
