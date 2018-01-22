package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.utils.emptyToNull
import net.pterodactylus.sone.utils.headers
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import javax.inject.Inject

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
