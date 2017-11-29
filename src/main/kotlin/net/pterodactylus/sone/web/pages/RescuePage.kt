package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
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
		LoggedInPage("rescue.html", template, "Page.Rescue.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext) {
		val soneRescuer = webInterface.core.getSoneRescuer(currentSone)
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
