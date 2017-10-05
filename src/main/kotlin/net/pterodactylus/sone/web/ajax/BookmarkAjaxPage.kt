package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.utils.also
import net.pterodactylus.sone.utils.emptyToNull
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * AJAX page that lets the user bookmark a post.
 */
class BookmarkAjaxPage(webInterface: WebInterface) : JsonPage("bookmark.ajax", webInterface) {

	override fun requiresLogin() = false

	override fun createJsonObject(request: FreenetRequest) =
			request.parameters["post"].emptyToNull
					?.let(core::getPost)
					?.also(core::bookmarkPost)
					?.let { createSuccessJsonObject() }
					?: createErrorJsonObject("invalid-post-id")

}
