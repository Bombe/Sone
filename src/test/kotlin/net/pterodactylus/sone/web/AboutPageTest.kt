package net.pterodactylus.sone.web

import net.pterodactylus.sone.main.SonePlugin.PluginHomepage
import net.pterodactylus.sone.main.SonePlugin.PluginVersion
import net.pterodactylus.sone.main.SonePlugin.PluginYear
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.isProvidedByMock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.sameInstance
import org.junit.Test

/**
 * Unit test for [AboutPage].
 */
class AboutPageTest : WebPageTest() {

	private val version = "0.1.2"
	private val year = 1234
	private val homepage = "home://page"
	private val page = AboutPage(template, webInterface, PluginVersion(version), PluginYear(year), PluginHomepage(homepage))
	private val childInjector = injector.createChildInjector(
			PluginVersion::class.isProvidedByMock(),
			PluginYear::class.isProvidedByMock(),
			PluginHomepage::class.isProvidedByMock()
	)!!

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("about.html"))
	}

	@Test
	fun `page does not require login`() {
		assertThat(page.requiresLogin(), equalTo(false))
	}

	@Test
	fun `page sets correct version in template context`() {
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["version"], equalTo<Any>(version))
	}

	@Test
	fun `page sets correct homepage in template context`() {
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["homepage"], equalTo<Any>(homepage))
	}

	@Test
	fun `page sets correct year in template context`() {
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["year"], equalTo<Any>(year))
	}

	@Test
	fun `page can be created by guice`() {
		assertThat(childInjector.getInstance<AboutPage>(), notNullValue())
	}

	@Test
	fun `page is created as singleton`() {
	    val firstInstance = childInjector.getInstance<AboutPage>()
		val secondInstance = childInjector.getInstance<AboutPage>()
		assertThat(firstInstance, sameInstance(secondInstance))
	}

}
