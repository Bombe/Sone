package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * AJAX page that lets the user distrust a Sone.
 *
 * @see Core.distrustSone(Sone, Sone)
 */
class DistrustAjaxPage(webInterface: WebInterface) : JsonPage("distrustSone.ajax", webInterface) {

	override fun createJsonObject(request: FreenetRequest) =
			request.parameters["sone"]
					.let(webInterface.core::getSone)
					?.let { sone ->
						createSuccessJsonObject()
								.put("trustValue", webInterface.core.preferences.negativeTrust)
								.also {
									webInterface.core.distrustSone(getCurrentSone(request.toadletContext), sone)
								}
					} ?: createErrorJsonObject("invalid-sone-id")

}
