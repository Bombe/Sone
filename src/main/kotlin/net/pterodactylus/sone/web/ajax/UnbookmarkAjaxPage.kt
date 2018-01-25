package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * AJAX page that lets the user unbookmark a post.
 */
class UnbookmarkAjaxPage(webInterface: WebInterface) : JsonPage("unbookmark.ajax", webInterface) {

	override val requiresLogin = false

	override fun createJsonObject(request: FreenetRequest) =
			request.parameters["post"]
					?.let(core::getPost)
					?.also(core::unbookmarkPost)
					?.let { createSuccessJsonObject() }
					?: createErrorJsonObject("invalid-post-id")

}
