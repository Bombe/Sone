package net.pterodactylus.sone.template

import com.google.inject.Guice
import net.pterodactylus.sone.core.LinkedElement
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.isProvidedByMock
import net.pterodactylus.util.template.ClassPathTemplateProvider
import net.pterodactylus.util.template.HtmlFilter
import net.pterodactylus.util.template.TemplateContextFactory
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.junit.Test

/**
 * Unit test for [LinkedElementRenderFilter].
 */
class LinkedElementRenderFilterTest {

	private val templateContextFactory = TemplateContextFactory()

	init {
		templateContextFactory.addFilter("html", HtmlFilter())
		templateContextFactory.addProvider(ClassPathTemplateProvider(LinkedElementRenderFilter::class.java, "/templates/"))
	}

	private val filter = LinkedElementRenderFilter(templateContextFactory)

	@Test
	fun `filter returns null for objects that are not linked elements`() {
		assertThat(filter.format(null, Any(), null), nullValue())
	}

	@Test
	fun `filter renders empty span for not loaded elements`() {
		val html = filter.format(null, LinkedElement("KSK@gpl.png", loading = true), emptyMap<String, Any?>()) as String
		val spanNode = Jsoup.parseBodyFragment(html).body().child(0)
		assertThat(spanNode.nodeName(), `is`("span"))
		assertThat(spanNode.attr("class"), `is`("linked-element not-loaded"))
		assertThat(spanNode.attr("title"), `is`("KSK@gpl.png"))
		assertThat(spanNode.hasAttr("style"), `is`(false))
		assertThat(spanNode.children().isEmpty(), `is`(true))
	}

	@Test
	fun `filter can render linked images`() {
		val html = filter.format(null, LinkedElement("KSK@gpl.png", properties = mapOf("type" to "image")), emptyMap<String, Any?>()) as String
		val outerSpanNode = Jsoup.parseBodyFragment(html).body().child(0)
		assertThat(outerSpanNode.nodeName(), `is`("span"))
		assertThat(outerSpanNode.attr("class"), `is`("linked-element loaded"))
		assertThat(outerSpanNode.attr("title"), `is`("KSK@gpl.png"))
		val linkNode = outerSpanNode.child(0)
		assertThat(linkNode.nodeName(), `is`("a"))
		assertThat(linkNode.attr("href"), `is`("/KSK@gpl.png"))
		val innerSpanNode = linkNode.child(0)
		assertThat(innerSpanNode.attr("style"), `is`("background-image: url('/KSK@gpl.png')"))
	}

	@Test
	fun `filter can render HTML pages`() {
		val html = filter.format(null, LinkedElement("KSK@gpl.html", properties = mapOf("type" to "html", "title" to "Page Title", "description" to "This is the description.")), emptyMap<String, Any?>()) as String
		val outerSpanNode = Jsoup.parseBodyFragment(html).body().child(0)
		assertThat(outerSpanNode.nodeName(), equalTo("span"))
		assertThat(outerSpanNode.attr("class"), `is`("linked-element loaded"))
		assertThat(outerSpanNode.attr("title"), `is`("KSK@gpl.html"))
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
