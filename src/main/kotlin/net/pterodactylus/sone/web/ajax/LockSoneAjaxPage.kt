package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * Lets the user [lock][net.pterodactylus.sone.core.Core.lockSone] a [Sone][net.pterodactylus.sone.data.Sone].
 */
class LockSoneAjaxPage(webInterface: WebInterface) : JsonPage("lockSone.ajax", webInterface) {

	override val requiresLogin = false

	override fun createJsonObject(request: FreenetRequest) =
			request.parameters["sone"]
					.let(core::getLocalSone)
					?.let(core::lockSone)
					?.let { createSuccessJsonObject() }
					?: createErrorJsonObject("invalid-sone-id")

}
