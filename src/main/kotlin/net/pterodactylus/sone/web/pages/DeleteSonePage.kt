package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Lets the user delete a Sone. Of course the Sone is not really deleted from
 * Freenet; merely all references to it are removed from the local plugin
 * installation.
 */
class DeleteSonePage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("deleteSone.html", template, "Page.DeleteSone.Title", webInterface, true) {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			if (freenetRequest.httpRequest.isPartSet("deleteSone")) {
				webInterface.core.deleteSone(getCurrentSone(freenetRequest.toadletContext))
			}
			throw RedirectException("index.html")
		}
	}

}
