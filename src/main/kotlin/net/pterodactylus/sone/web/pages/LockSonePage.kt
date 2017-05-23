package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * This page lets the user lock a [net.pterodactylus.sone.data.Sone] to prevent it from being inserted.
 */
class LockSonePage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("lockSone.html", template, "Page.LockSone.Title", webInterface, false) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		request.parameters["returnPage", 256]!!.let { returnPage ->
			request.parameters["sone", 44]!!
					.let { webInterface.core.getLocalSone(it) }
					?.let { webInterface.core.lockSone(it) }
			throw RedirectException(returnPage)
		}
	}

}
