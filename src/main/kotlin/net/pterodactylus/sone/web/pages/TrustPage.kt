package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Page that lets the user trust another Sone. This will assign a configurable
 * amount of trust to an identity.
 */
@ToadletPath("trust.html")
class TrustPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("Page.Trust.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			soneRequest.core.getSone(soneRequest.parameters["sone"]!!)?.let { sone ->
				soneRequest.core.trustSone(currentSone, sone)
			}
			throw RedirectException(soneRequest.parameters["returnPage", 256])
		}
	}

}
