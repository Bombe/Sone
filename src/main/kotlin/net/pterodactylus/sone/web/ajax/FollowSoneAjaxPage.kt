package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.also
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * AJAX page that lets a Sone follow another Sone.
 */
class FollowSoneAjaxPage(webInterface: WebInterface) : LoggedInJsonPage("followSone.ajax", webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			request.parameters["sone"]
					?.let(core::getSone)
					?.also { core.followSone(currentSone, it.id) }
					?.also(core::markSoneKnown)
					?.let { createSuccessJsonObject() }
					?: createErrorJsonObject("invalid-sone-id")

}
