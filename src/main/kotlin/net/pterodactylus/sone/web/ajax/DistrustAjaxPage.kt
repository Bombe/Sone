package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * AJAX page that lets the user distrust a Sone.
 *
 * @see Core.distrustSone(Sone, Sone)
 */
class DistrustAjaxPage(webInterface: WebInterface) : LoggedInJsonPage("distrustSone.ajax", webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			request.parameters["sone"]
					.let(core::getSone)
					?.let { sone ->
						createSuccessJsonObject()
								.put("trustValue", core.preferences.negativeTrust)
								.also {
									core.distrustSone(currentSone, sone)
								}
					} ?: createErrorJsonObject("invalid-sone-id")

}
