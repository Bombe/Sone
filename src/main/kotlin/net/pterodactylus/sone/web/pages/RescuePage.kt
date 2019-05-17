package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Page that lets the user control the rescue mode for a Sone.
 */
@MenuName("Rescue")
@TemplatePath("/templates/rescue.html")
class RescuePage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer):
		LoggedInPage("rescue.html", "Page.Rescue.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		val soneRescuer = soneRequest.core.getSoneRescuer(currentSone)
		templateContext["soneRescuer"] = soneRescuer
		if (soneRequest.isPOST) {
			soneRequest.parameters["edition", 9]?.toIntOrNull()?.also {
				if (it > -1) {
					soneRescuer.setEdition(it.toLong())
				}
			}
			if (soneRequest.parameters["fetch", 8] == "true") {
				soneRescuer.startNextFetch()
			}
			throw RedirectException("rescue.html")
		}
	}

}
