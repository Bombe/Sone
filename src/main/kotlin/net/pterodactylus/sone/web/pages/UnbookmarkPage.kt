package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.utils.also
import net.pterodactylus.sone.utils.isGET
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user unbookmark a post.
 */
class UnbookmarkPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("unbookmark.html", template, "Page.Unbookmark.Title", webInterface, false) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		when {
			request.isGET && (request.parameters["post"] == "allNotLoaded") -> {
				webInterface.core.bookmarkedPosts
						.filterNot(Post::isLoaded)
						.forEach(webInterface.core::unbookmarkPost)
				throw RedirectException("bookmarks.html")
			}
			request.isPOST -> {
				request.parameters["post", 36]
						.let(webInterface.core::getPost)
						.also(webInterface.core::unbookmarkPost)
				throw RedirectException(request.parameters["returnPage", 256])
			}
		}
	}

}
