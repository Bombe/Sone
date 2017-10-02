package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * AJAX page that lets the user trust a Sone.
 *
 * @see net.pterodactylus.sone.core.Core.trustSone
 */
class TrustAjaxPage(webInterface: WebInterface) : LoggedInJsonPage("trustSone.ajax", webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			request.parameters["sone"]
					.let(webInterface.core::getSone)
					?.let { webInterface.core.trustSone(currentSone, it) }
					?.let { createSuccessJsonObject().put("trustValue", webInterface.core.preferences.positiveTrust) }
					?: createErrorJsonObject("invalid-sone-id")

}
