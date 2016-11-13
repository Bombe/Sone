package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.LinkedElement
import net.pterodactylus.util.template.HtmlFilter
import net.pterodactylus.util.template.TemplateContextFactory
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.jsoup.Jsoup
import org.junit.Test

/**
 * Unit test for [LinkedElementRenderFilter].
 */
class LinkedElementRenderFilterTest {

	private val templateContextFactory = TemplateContextFactory()

	init {
		templateContextFactory.addFilter("html", HtmlFilter())
	}

	private val filter = LinkedElementRenderFilter(templateContextFactory)

	@Test
	fun `filter can render linked images`() {
		val html = filter.format(null, LinkedElement("KSK@gpl.png"), emptyMap<String, Any?>()) as String
		val linkNode = Jsoup.parseBodyFragment(html).body().child(0)
		assertThat(linkNode.nodeName(), `is`("a"))
		assertThat(linkNode.attr("href"), `is`("/KSK@gpl.png"))
		val spanNode = linkNode.child(0)
		assertThat(spanNode.nodeName(), `is`("span"))
		assertThat(spanNode.attr("class"), `is`("linked-element"))
		assertThat(spanNode.attr("title"), `is`("KSK@gpl.png"))
		assertThat(spanNode.attr("style"), `is`("background-image: url('/KSK@gpl.png')"))
	}

}
