package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import javax.inject.Inject

/**
 * Lets the user [unlock][net.pterodactylus.sone.core.Core.unlockSone] a [Sone][net.pterodactylus.sone.data.Sone].
 */
@ToadletPath("unlockSone.ajax")
class UnlockSoneAjaxPage @Inject constructor(webInterface: WebInterface) : JsonPage(webInterface) {

	override val requiresLogin = false

	override fun createJsonObject(request: FreenetRequest) =
			request.parameters["sone"]
					?.let(core::getLocalSone)
					?.also(core::unlockSone)
					?.also { core.touchConfiguration() }
					?.let { createSuccessJsonObject() }
					?: createErrorJsonObject("invalid-sone-id")

}
