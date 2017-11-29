package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user trust another Sone. This will assign a configurable
 * amount of trust to an identity.
 */
class TrustPage(template: Template, webInterface: WebInterface):
		LoggedInPage("trust.html", template, "Page.Trust.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
				webInterface.core.getSone(freenetRequest.parameters["sone"]!!)?.let { sone ->
					webInterface.core.trustSone(currentSone, sone)
				}
			throw RedirectException(freenetRequest.parameters["returnPage", 256])
		}
	}

}
