package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * A [SoneTemplatePage] that stores information about Sone in the [TemplateContext].
 */
@MenuName("About")
@TemplatePath("/templates/about.html")
@ToadletPath("about.html")
class AboutPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer,
		private val pluginVersion: PluginVersion,
		private val pluginYear: PluginYear,
		private val pluginHomepage: PluginHomepage) : SoneTemplatePage(webInterface, loaders, templateRenderer, pageTitleKey = "Page.About.Title") {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		templateContext["version"] = pluginVersion.version
		templateContext["year"] = pluginYear.year
		templateContext["homepage"] = pluginHomepage.homepage
	}

}
