package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Base class for [SoneTemplatePage] implementations that require a logged in user.
 */
abstract class LoggedInPage(path: String, template: Template, pageTitleKey: String, webInterface: WebInterface, loaders: Loaders) :
		SoneTemplatePage(path, webInterface, loaders, template = template, pageTitleKey = pageTitleKey, requiresLogin = true) {

	final override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		handleRequest(soneRequest, getCurrentSone(soneRequest.toadletContext, false)!!, templateContext)
	}

	protected abstract fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext)

}
