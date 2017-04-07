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
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.jsoup.Jsoup
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
	fun `filter renders loading animation for not loaded elements`() {
		val html = filter.format(null, LinkedElement("KSK@gpl.png", loading = true), emptyMap<String, Any?>()) as String
		val spanNode = Jsoup.parseBodyFragment(html).body().child(0)
		assertThat(spanNode.nodeName(), `is`("span"))
		assertThat(spanNode.attr("class"), `is`("linked-element"))
		assertThat(spanNode.attr("title"), `is`("KSK@gpl.png"))
		assertThat(spanNode.attr("style"), `is`("background-image: url('images/loading-animation.gif')"))
	}

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

	@Test
	fun `render filter can be created by guice`() {
	    val injector = Guice.createInjector(TemplateContextFactory::class.isProvidedByMock())
		assertThat(injector.getInstance<LinkedElementRenderFilter>(), notNullValue())
	}

}
