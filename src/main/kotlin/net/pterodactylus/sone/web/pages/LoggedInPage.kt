package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*

/**
 * Base class for [SoneTemplatePage] implementations that require a logged in user.
 */
abstract class LoggedInPage(path: String, pageTitleKey: String, webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		SoneTemplatePage(path, webInterface, loaders, templateRenderer, pageTitleKey = pageTitleKey, requiresLogin = true) {

	final override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		handleRequest(soneRequest, getCurrentSone(soneRequest.toadletContext, false)!!, templateContext)
	}

	protected abstract fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext)

}
