package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Base class for [SoneTemplatePage] implementations that require a logged in user.
 */
abstract class LoggedInPage(path: String, template: Template, pageTitleKey: String, webInterface: WebInterface) :
		SoneTemplatePage(path, webInterface, template, pageTitleKey, true) {

	final override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		handleRequest(freenetRequest, getCurrentSone(freenetRequest.toadletContext, false)!!, templateContext)
	}

	protected abstract fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext)

}
