package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.text.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import javax.inject.*

/**
 * This AJAX page create a reply.
 */
class CreateReplyAjaxPage @Inject constructor(webInterface: WebInterface) : LoggedInJsonPage("createReply.ajax", webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest): JsonReturnObject =
			request.parameters["post"].emptyToNull
					?.let(core::getPost)
					?.let { post ->
						val text = TextFilter.filter(request.headers["Host"], request.parameters["text"])
						val sender = request.parameters["sender"].let(core::getLocalSone) ?: currentSone
						val reply = core.createReply(sender, post, text)
						createSuccessJsonObject().apply {
							put("reply", reply.id)
							put("sone", sender.id)
						}
					}
					?: createErrorJsonObject("invalid-post-id")

}
