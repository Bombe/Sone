package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * A [SoneTemplatePage] that stores information about Sone in the [TemplateContext].
 */
@MenuName("About")
class AboutPage @Inject constructor(template: Template, webInterface: WebInterface,
		private val pluginVersion: PluginVersion,
		private val pluginYear: PluginYear,
		private val pluginHomepage: PluginHomepage): SoneTemplatePage("about.html", webInterface, template, "Page.About.Title") {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		templateContext["version"] = pluginVersion.version
		templateContext["year"] = pluginYear.year
		templateContext["homepage"] = pluginHomepage.homepage
	}

}
