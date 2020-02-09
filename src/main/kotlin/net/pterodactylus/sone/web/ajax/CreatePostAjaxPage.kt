package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.text.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import javax.inject.*

/**
 * AJAX handler that creates a new post.
 */
@ToadletPath("createPost.ajax")
class CreatePostAjaxPage @Inject constructor(webInterface: WebInterface) : LoggedInJsonPage(webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			request.parameters["text"].emptyToNull
					?.let { TextFilter.filter(request.headers["Host"], it) }
					?.let { text ->
						val sender = request.parameters["sender"].emptyToNull?.let(core::getSone) ?: currentSone
						val recipient = request.parameters["recipient"]?.let(core::getSone)
						core.createPost(sender, recipient, text).let { post ->
							createSuccessJsonObject().apply {
								put("postId", post.id)
								put("sone", sender.id)
								put("recipient", recipient?.id)
							}
						}
					} ?: createErrorJsonObject("text-required")

}
