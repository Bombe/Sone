package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * This page lets the user follow another Sone.
 */
class FollowSonePage @Inject constructor(template: Template, webInterface: WebInterface, loaders: Loaders):
		LoggedInPage("followSone.html", template, "Page.FollowSone.Title", webInterface, loaders) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			soneRequest.httpRequest.getPartAsStringFailsafe("sone", 1200).split(Regex("[ ,]+"))
					.map { it to soneRequest.core.getSone(it) }
					.filterNot { it.second == null }
					.forEach { sone ->
						soneRequest.core.followSone(currentSone, sone.first)
						soneRequest.core.markSoneKnown(sone.second)
					}
			throw RedirectException(soneRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256))
		}
	}

}
