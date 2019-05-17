package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.isProvidedByMock
import net.pterodactylus.sone.web.baseInjector
import net.pterodactylus.sone.web.page.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test

/**
 * Unit test for [AboutPage].
 */
class AboutPageTest : WebPageTest({ webInterface, loaders, templateRenderer -> AboutPage(webInterface, loaders, templateRenderer, PluginVersion(version), PluginYear(year), PluginHomepage(homepage)) }) {

	companion object {
		private const val version = "0.1.2"
		private const val year = 1234
		private const val homepage = "home://page"
	}

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
	fun `about page can be created by dependency injection`() {
		val injector = baseInjector.createChildInjector(
				PluginVersion::class.isProvidedByMock(),
				PluginYear::class.isProvidedByMock(),
				PluginHomepage::class.isProvidedByMock()
		)
		assertThat(injector.getInstance<AboutPage>(), notNullValue())
	}

	@Test
	fun `page is annotated with correct menuname`() {
	    assertThat(page.menuName, equalTo("About"))
	}

	@Test
	fun `page is annotated with correct template path`() {
	    assertThat(page.templatePath, equalTo("/templates/about.html"))
	}

}
