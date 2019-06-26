package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * This page lets the user follow another Sone.
 */
@ToadletPath("followSone.html")
class FollowSonePage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("Page.FollowSone.Title", webInterface, loaders, templateRenderer) {

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
