package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import javax.inject.*

/**
 * This AJAX page deletes a reply.
 */
class DeleteReplyAjaxPage @Inject constructor(webInterface: WebInterface) :
		LoggedInJsonPage("deleteReply.ajax", webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			request.parameters["reply"]
					?.let(core::getPostReply)
					?.let { reply ->
						reply.sone.isLocal.ifTrue {
							createSuccessJsonObject().also {
								core.deleteReply(reply)
							}
						} ?: createErrorJsonObject("not-authorized")
					} ?: createErrorJsonObject("invalid-reply-id")

}
