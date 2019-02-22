package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import javax.inject.*

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
