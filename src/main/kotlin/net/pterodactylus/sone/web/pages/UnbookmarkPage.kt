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
		SoneTemplatePage("unbookmark.html", webInterface, template, "Page.Unbookmark.Title") {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		when {
			freenetRequest.isGET && (freenetRequest.parameters["post"] == "allNotLoaded") -> {
				webInterface.core.bookmarkedPosts
						.filterNot(Post::isLoaded)
						.forEach(webInterface.core::unbookmarkPost)
				throw RedirectException("bookmarks.html")
			}
			freenetRequest.isPOST -> {
				freenetRequest.parameters["post", 36]
						?.let(webInterface.core::getPost)
						?.also(webInterface.core::unbookmarkPost)
				throw RedirectException(freenetRequest.parameters["returnPage", 256])
			}
		}
	}

}
