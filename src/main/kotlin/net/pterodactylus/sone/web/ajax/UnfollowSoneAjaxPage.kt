package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import javax.inject.Inject

/**
 * AJAX page that lets a Sone unfollow another Sone.
 */
@ToadletPath("unfollowSone.ajax")
class UnfollowSoneAjaxPage @Inject constructor(webInterface: WebInterface) : LoggedInJsonPage(webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			request.parameters["sone"]
					?.let(core::getSone)
					?.also { core.unfollowSone(currentSone, it.id) }
					?.let { createSuccessJsonObject() }
					?: createErrorJsonObject("invalid-sone-id")

}
