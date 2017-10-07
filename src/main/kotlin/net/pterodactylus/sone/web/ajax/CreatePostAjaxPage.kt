package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.utils.emptyToNull
import net.pterodactylus.sone.utils.headers
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * AJAX handler that creates a new post.
 */
class CreatePostAjaxPage(webInterface: WebInterface) : LoggedInJsonPage("createPost.ajax", webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			request.parameters["text"].emptyToNull
					?.let { TextFilter.filter(request.headers["Host"], it) }
					?.let { text ->
						val sender = request.parameters["sender"].emptyToNull?.let(core::getSone)?.orNull() ?: currentSone
						val recipient = request.parameters["recipient"].let(core::getSone)
						core.createPost(sender, recipient, text).let { post ->
							createSuccessJsonObject().apply {
								put("postId", post.id)
								put("sone", sender.id)
								put("recipient", recipient.let(Sone::getId))
							}
						}
					} ?: createErrorJsonObject("text-required")

}
