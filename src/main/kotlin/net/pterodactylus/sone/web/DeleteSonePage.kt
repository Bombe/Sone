package net.pterodactylus.sone.web

import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import net.pterodactylus.util.web.Method.POST

/**
 * Lets the user delete a Sone. Of course the Sone is not really deleted from
 * Freenet; merely all references to it are removed from the local plugin
 * installation.
 */
class DeleteSonePage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("deleteSone.html", template, "Page.DeleteSone.Title", webInterface, true) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		if (request.method == POST) {
			if (request.httpRequest.isPartSet("deleteSone")) {
				webInterface.core.deleteSone(getCurrentSone(request.toadletContext))
			}
			throw RedirectException("index.html")
		}
	}

}
