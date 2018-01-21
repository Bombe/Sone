package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.utils.emptyToNull
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import javax.inject.Inject

/**
 * AJAX page that lets the user bookmark a post.
 */
class BookmarkAjaxPage @Inject constructor(webInterface: WebInterface) : JsonPage("bookmark.ajax", webInterface) {

	override val requiresLogin = false

	override fun createJsonObject(request: FreenetRequest) =
			request.parameters["post"].emptyToNull
					?.let { postId ->
						core.getPost(postId)?.also(core::bookmarkPost)
						createSuccessJsonObject()
					}
					?: createErrorJsonObject("invalid-post-id")

}
