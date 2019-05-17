package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Page that lets the user trust another Sone. This will assign a configurable
 * amount of trust to an identity.
 */
class TrustPage @Inject constructor(template: Template, webInterface: WebInterface, loaders: Loaders) :
		LoggedInPage("trust.html", template, "Page.Trust.Title", webInterface, loaders) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			soneRequest.core.getSone(soneRequest.parameters["sone"]!!)?.let { sone ->
				soneRequest.core.trustSone(currentSone, sone)
			}
			throw RedirectException(soneRequest.parameters["returnPage", 256])
		}
	}

}
