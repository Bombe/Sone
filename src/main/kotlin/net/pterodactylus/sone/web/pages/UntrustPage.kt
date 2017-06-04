package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.utils.also
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user untrust another Sone. This will remove all trust
 * assignments for an identity.
 */
class UntrustPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("untrust.html", template, "Page.Untrust.Title", webInterface, true) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		if (request.isPOST) {
			getCurrentSone(request.toadletContext)!!.also { currentSone ->
				request.parameters["sone", 44]
						.let(webInterface.core::getSone)
						.also { webInterface.core.untrustSone(currentSone, it) }
			}
			throw RedirectException(request.parameters["returnPage", 256])
		}
	}

}
