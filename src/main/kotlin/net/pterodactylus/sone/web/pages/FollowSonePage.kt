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

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			freenetRequest.httpRequest.getPartAsStringFailsafe("sone", 1200).split(Regex("[ ,]+"))
					.map { it to webInterface.core.getSone(it) }
					.filterNot { it.second == null }
					.forEach { sone ->
						webInterface.core.followSone(freenetRequest.currentSone, sone.first)
						webInterface.core.markSoneKnown(sone.second)
					}
			throw RedirectException(freenetRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256))
		}
	}

	private val FreenetRequest.currentSone get() = sessionProvider.getCurrentSone(toadletContext)

}
