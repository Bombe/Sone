package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.utils.ifTrue
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * This AJAX page deletes a post.
 */
class DeletePostAjaxPage(webInterface: WebInterface) : JsonPage("deletePost.ajax", webInterface) {

	override fun createJsonObject(request: FreenetRequest) =
			request.parameters["post"]
					.let(webInterface.core::getPost)
					?.let { post ->
						post.sone.isLocal.ifTrue {
							createSuccessJsonObject().also {
								webInterface.core.deletePost(post)
							}
						} ?: createErrorJsonObject("not-authorized")
					} ?: createErrorJsonObject("invalid-post-id")

}
