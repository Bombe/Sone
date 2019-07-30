package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Page that lets the user control the rescue mode for a Sone.
 */
@MenuName("Rescue")
@TemplatePath("/templates/rescue.html")
@ToadletPath("rescue.html")
class RescuePage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("Page.Rescue.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		val soneRescuer = soneRequest.core.getSoneRescuer(currentSone)
		templateContext["soneRescuer"] = soneRescuer
		if (soneRequest.isPOST) {
			if (soneRequest.parameters["fetch", 8] == "true") {
				soneRescuer.startNextFetch()
			}
			redirectTo("rescue.html")
		}
	}

}
