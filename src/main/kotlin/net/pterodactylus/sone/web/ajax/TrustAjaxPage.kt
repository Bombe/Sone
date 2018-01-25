package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import javax.inject.Inject

/**
 * AJAX page that lets the user trust a Sone.
 *
 * @see net.pterodactylus.sone.core.Core.trustSone
 */
class TrustAjaxPage @Inject constructor(webInterface: WebInterface) :
		LoggedInJsonPage("trustSone.ajax", webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			request.parameters["sone"]
					?.let(core::getSone)
					?.let { core.trustSone(currentSone, it) }
					?.let { createSuccessJsonObject().put("trustValue", core.preferences.positiveTrust) }
					?: createErrorJsonObject("invalid-sone-id")

}
