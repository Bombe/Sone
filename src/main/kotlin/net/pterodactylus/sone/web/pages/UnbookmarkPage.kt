package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Page that lets the user unbookmark a post.
 */
class UnbookmarkPage @Inject constructor(template: Template, webInterface: WebInterface):
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
