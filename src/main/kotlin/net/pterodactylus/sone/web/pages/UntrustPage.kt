package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Page that lets the user untrust another Sone. This will remove all trust
 * assignments for an identity.
 */
class UntrustPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("untrust.html", "Page.Untrust.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			soneRequest.parameters["sone", 44]!!
					.let(soneRequest.core::getSone)
					?.also { soneRequest.core.untrustSone(currentSone, it) }
			throw RedirectException(soneRequest.parameters["returnPage", 256])
		}
	}

}
