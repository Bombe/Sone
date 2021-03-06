package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import javax.inject.Inject

/**
 * AJAX page that lets the user unbookmark a post.
 */
@ToadletPath("unbookmark.ajax")
class UnbookmarkAjaxPage @Inject constructor(webInterface: WebInterface) : JsonPage(webInterface) {

	override val requiresLogin = false

	override fun createJsonObject(request: FreenetRequest) =
			request.parameters["post"]
					?.let(core::getPost)
					?.also(core::unbookmarkPost)
					?.let { createSuccessJsonObject() }
					?: createErrorJsonObject("invalid-post-id")

}
