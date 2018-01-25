package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import javax.inject.Inject

/**
 * AJAX page that lets the user mark a number of [net.pterodactylus.sone.data.Sone]s,
 * [net.pterodactylus.sone.data.Post]s, or [net.pterodactylus.sone.data.Reply]s as known.
 */
class MarkAsKnownAjaxPage @Inject constructor(webInterface: WebInterface) :
		JsonPage("markAsKnown.ajax", webInterface) {

	override val requiresLogin = false

	override fun createJsonObject(request: FreenetRequest) = when (request.parameters["type"]) {
		"sone" -> processIds(request, core::getSone, core::markSoneKnown)
		"post" -> processIds(request, core::getPost, core::markPostKnown)
		"reply" -> processIds(request, core::getPostReply, core::markReplyKnown)
		else -> createErrorJsonObject("invalid-type")
	}

	private fun <T : Any> processIds(request: FreenetRequest, getter: (String) -> T?, marker: (T) -> Unit) =
			request.parameters["id"]
					?.split(Regex(" +"))
					?.mapNotNull(getter)
					?.onEach(marker)
					.let { createSuccessJsonObject() }

}
