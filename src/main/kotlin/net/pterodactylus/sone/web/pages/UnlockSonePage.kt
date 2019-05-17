package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * This page lets the user unlock a [net.pterodactylus.sone.data.Sone] to allow its insertion.
 */
class UnlockSonePage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer):
		SoneTemplatePage("unlockSone.html", webInterface, loaders, templateRenderer, pageTitleKey = "Page.UnlockSone.Title") {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			soneRequest.parameters["sone", 44]
					.let(soneRequest.core::getLocalSone)
					?.also(soneRequest.core::unlockSone)
			throw RedirectException(soneRequest.parameters["returnPage", 256])
		}
	}

}
