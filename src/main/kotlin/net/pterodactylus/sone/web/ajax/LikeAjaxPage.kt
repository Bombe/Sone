package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import javax.inject.Inject

/**
 * AJAX page that lets the user like a [net.pterodactylus.sone.data.Post].
 */
@ToadletPath("like.ajax")
class LikeAjaxPage @Inject constructor(webInterface: WebInterface) : LoggedInJsonPage(webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			when (request.parameters["type"]) {
				"post" -> request.parameters["post"]
						?.let(core::getPost)
						?.let { currentSone.addLikedPostId(it.id) }
						?.also { core.touchConfiguration() }
						?.let { createSuccessJsonObject() }
						?: createErrorJsonObject("invalid-post-id")
				"reply" -> request.parameters["reply"]
						?.let(core::getPostReply)
						?.let { currentSone.addLikedReplyId(it.id) }
						?.also { core.touchConfiguration() }
						?.let { createSuccessJsonObject() }
						?: createErrorJsonObject("invalid-reply-id")
				else -> createErrorJsonObject("invalid-type")
			}


}
