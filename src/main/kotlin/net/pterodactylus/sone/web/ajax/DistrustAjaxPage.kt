package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import javax.inject.*

/**
 * AJAX page that lets the user distrust a Sone.
 *
 * @see Core.distrustSone(Sone, Sone)
 */
class DistrustAjaxPage @Inject constructor(webInterface: WebInterface) :
		LoggedInJsonPage("distrustSone.ajax", webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			request.parameters["sone"]
					?.let(core::getSone)
					?.let { sone ->
						createSuccessJsonObject()
								.put("trustValue", core.preferences.negativeTrust)
								.also {
									core.distrustSone(currentSone, sone)
								}
					} ?: createErrorJsonObject("invalid-sone-id")

}
