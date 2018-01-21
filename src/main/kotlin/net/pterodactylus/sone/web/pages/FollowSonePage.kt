package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * This page lets the user follow another Sone.
 */
class FollowSonePage @Inject constructor(template: Template, webInterface: WebInterface):
		LoggedInPage("followSone.html", template, "Page.FollowSone.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			freenetRequest.httpRequest.getPartAsStringFailsafe("sone", 1200).split(Regex("[ ,]+"))
					.map { it to webInterface.core.getSone(it) }
					.filterNot { it.second == null }
					.forEach { sone ->
						webInterface.core.followSone(currentSone, sone.first)
						webInterface.core.markSoneKnown(sone.second)
					}
			throw RedirectException(freenetRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256))
		}
	}

}
