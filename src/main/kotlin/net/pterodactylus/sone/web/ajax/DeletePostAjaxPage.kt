package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.ifTrue
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import javax.inject.Inject

/**
 * This AJAX page deletes a post.
 */
class DeletePostAjaxPage @Inject constructor(webInterface: WebInterface) : LoggedInJsonPage("deletePost.ajax", webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			request.parameters["post"]
					?.let(core::getPost)
					?.let { post ->
						post.sone.isLocal.ifTrue {
							createSuccessJsonObject().also {
								core.deletePost(post)
							}
						} ?: createErrorJsonObject("not-authorized")
					} ?: createErrorJsonObject("invalid-post-id")

}
