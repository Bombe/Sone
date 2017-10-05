package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * Lets the user [unlock][net.pterodactylus.sone.core.Core.unlockSone] a [Sone][net.pterodactylus.sone.data.Sone].
 */
class UnlockSoneAjaxPage(webInterface: WebInterface) : JsonPage("unlockSone.ajax", webInterface) {

	override fun requiresLogin() = false

	override fun createJsonObject(request: FreenetRequest) =
			request.parameters["sone"]
					?.let(core::getLocalSone)
					?.also(core::unlockSone)
					?.also { core.touchConfiguration() }
					?.let { createSuccessJsonObject() }
					?: createErrorJsonObject("invalid-sone-id")

}
