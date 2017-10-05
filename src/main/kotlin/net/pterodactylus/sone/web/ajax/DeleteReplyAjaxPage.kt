package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.ifTrue
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * This AJAX page deletes a reply.
 */
class DeleteReplyAjaxPage(webInterface: WebInterface) : LoggedInJsonPage("deleteReply.ajax", webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			request.parameters["reply"]
					.let(core::getPostReply)
					?.let { reply ->
						reply.sone.isLocal.ifTrue {
							createSuccessJsonObject().also {
								core.deleteReply(reply)
							}
						} ?: createErrorJsonObject("not-authorized")
					} ?: createErrorJsonObject("invalid-reply-id")

}
