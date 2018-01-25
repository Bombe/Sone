package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.emptyToNull
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import javax.inject.Inject

/**
 * AJAX page that lets the user unlike a [net.pterodactylus.sone.data.Post].
 */
class UnlikeAjaxPage @Inject constructor(webInterface: WebInterface) :
		LoggedInJsonPage("unlike.ajax", webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) = when (request.parameters["type"]) {
		"post" -> request.processEntity("post", currentSone::removeLikedPostId)
		"reply" -> request.processEntity("reply", currentSone::removeLikedReplyId)
		else -> createErrorJsonObject("invalid-type")
	}

	private fun FreenetRequest.processEntity(entity: String, likeRemover: (String) -> Unit) =
			parameters[entity].emptyToNull
					?.also(likeRemover)
					?.also { core.touchConfiguration() }
					?.let { createSuccessJsonObject() }
					?: createErrorJsonObject("invalid-$entity-id")

}
