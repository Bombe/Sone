package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * This page lets the user follow another Sone.
 */
class FollowSonePage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("followSone.html", template, "Page.FollowSone.Title", webInterface, true) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		if (request.isPOST) {
			request.httpRequest.getPartAsStringFailsafe("sone", 1200).split(Regex("[ ,]+"))
					.map { it to webInterface.core.getSone(it) }
					.filter { it.second.isPresent }
					.map { it.first to it.second.get() }
					.forEach { sone ->
						webInterface.core.followSone(request.currentSone, sone.first)
						webInterface.core.markSoneKnown(sone.second)
					}
			throw RedirectException(request.httpRequest.getPartAsStringFailsafe("returnPage", 256))
		}
	}

	private val FreenetRequest.currentSone get() = sessionProvider.getCurrentSone(toadletContext)

}
