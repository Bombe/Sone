package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * This page lets the user unlock a [net.pterodactylus.sone.data.Sone] to allow its insertion.
 */
class UnlockSonePage @Inject constructor(template: Template, webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer):
		SoneTemplatePage("unlockSone.html", webInterface, loaders, template, templateRenderer, pageTitleKey = "Page.UnlockSone.Title") {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			soneRequest.parameters["sone", 44]
					.let(soneRequest.core::getLocalSone)
					?.also(soneRequest.core::unlockSone)
			throw RedirectException(soneRequest.parameters["returnPage", 256])
		}
	}

}
