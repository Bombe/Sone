package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import javax.inject.*

/**
 * This AJAX page deletes a post.
 */
@ToadletPath("deletePost.ajax")
class DeletePostAjaxPage @Inject constructor(webInterface: WebInterface) : LoggedInJsonPage(webInterface) {

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
