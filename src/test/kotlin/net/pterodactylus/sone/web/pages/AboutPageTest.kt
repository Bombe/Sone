package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.main.SonePlugin.PluginHomepage
import net.pterodactylus.sone.main.SonePlugin.PluginVersion
import net.pterodactylus.sone.main.SonePlugin.PluginYear
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [AboutPage].
 */
class AboutPageTest: WebPageTest({ template, webInterface -> AboutPage(template, webInterface, PluginVersion(version), PluginYear(year), PluginHomepage(homepage)) }) {

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

}
