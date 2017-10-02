package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.utils.also
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * AJAX page that lets the user unbookmark a post.
 */
class UnbookmarkAjaxPage(webInterface: WebInterface) : JsonPage("unbookmark.ajax", webInterface) {

	override fun requiresLogin() = false

	override fun createJsonObject(request: FreenetRequest) =
			request.parameters["post"]
					?.let(webInterface.core::getPost)
					?.also(webInterface.core::unbookmarkPost)
					?.let { createSuccessJsonObject() }
					?: createErrorJsonObject("invalid-post-id")

}
