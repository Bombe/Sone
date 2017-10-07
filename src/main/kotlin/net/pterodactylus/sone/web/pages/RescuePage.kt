package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user control the rescue mode for a Sone.
 */
class RescuePage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("rescue.html", template, "Page.Rescue.Title", webInterface, true) {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		val soneRescuer = webInterface.core.getSoneRescuer(getCurrentSone(freenetRequest.toadletContext)!!)
		templateContext["soneRescuer"] = soneRescuer
		if (freenetRequest.isPOST) {
			freenetRequest.parameters["edition", 9]?.toIntOrNull()?.also {
				if (it > -1) {
					soneRescuer.setEdition(it.toLong())
				}
			}
			if (freenetRequest.parameters["fetch", 8] == "true") {
				soneRescuer.startNextFetch()
			}
			throw RedirectException("rescue.html")
		}
	}

}
