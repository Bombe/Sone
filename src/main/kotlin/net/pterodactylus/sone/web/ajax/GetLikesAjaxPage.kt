package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.template.SoneAccessor
import net.pterodactylus.sone.utils.jsonArray
import net.pterodactylus.sone.utils.jsonObject
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * AJAX page that retrieves the number of “likes” a [net.pterodactylus.sone.data.Post]
 * or [net.pterodactylus.sone.data.PostReply] has.
 */
class GetLikesAjaxPage(webInterface: WebInterface) : JsonPage("getLikes.ajax", webInterface) {

	override fun needsFormPassword() = false

	override fun createJsonObject(request: FreenetRequest) =
			when (request.parameters["type"]) {
				"post" -> request.parameters["post"]
						.let(core::getPost)
						?.let(core::getLikes)
						?.toReply()
						?: createErrorJsonObject("invalid-post-id")
				"reply" -> request.parameters["reply"]
						.let(core::getPostReply)
						?.let(core::getLikes)
						?.toReply()
						?: createErrorJsonObject("invalid-reply-id")
				else -> createErrorJsonObject("invalid-type")
			}

	private fun Set<Sone>.toReply() = createSuccessJsonObject().apply {
		put("likes", size)
		put("sones", sortedBy { SoneAccessor.getNiceName(it) }
				.map {
					jsonObject("id" to it.id, "name" to SoneAccessor.getNiceName(it))
				}
				.let { jsonArray(*it.toTypedArray()) }
		)
	}

}
