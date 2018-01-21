package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Page that lets the user untrust another Sone. This will remove all trust
 * assignments for an identity.
 */
class UntrustPage @Inject constructor(template: Template, webInterface: WebInterface) :
		LoggedInPage("untrust.html", template, "Page.Untrust.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			freenetRequest.parameters["sone", 44]!!
					.let(webInterface.core::getSone)
					?.also { webInterface.core.untrustSone(currentSone, it) }
			throw RedirectException(freenetRequest.parameters["returnPage", 256])
		}
	}

}
