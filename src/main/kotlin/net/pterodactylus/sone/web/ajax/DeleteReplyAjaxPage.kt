package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.utils.ifTrue
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * This AJAX page deletes a reply.
 */
class DeleteReplyAjaxPage(webInterface: WebInterface) : JsonPage("deleteReply.ajax", webInterface) {

	override fun createJsonObject(request: FreenetRequest) =
			request.parameters["reply"]
					.let(webInterface.core::getPostReply)
					?.let { reply ->
						reply.sone.isLocal.ifTrue {
							createSuccessJsonObject().also {
								webInterface.core.deleteReply(reply)
							}
						} ?: createErrorJsonObject("not-authorized")
					} ?: createErrorJsonObject("invalid-reply-id")

}
