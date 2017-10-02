package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.also
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * AJAX page that lets the user [untrust][net.pterodactylus.sone.core.Core.untrustSone] a [Sone].
 */
class UntrustAjaxPage(webInterface: WebInterface) : LoggedInJsonPage("untrustSone.ajax", webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			request.parameters["sone"]
					?.let(webInterface.core::getSone)
					?.also { webInterface.core.untrustSone(currentSone, it) }
					?.let { createSuccessJsonObject() }
					?: createErrorJsonObject("invalid-sone-id")

}
