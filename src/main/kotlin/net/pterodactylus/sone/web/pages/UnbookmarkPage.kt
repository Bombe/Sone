package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Page that lets the user unbookmark a post.
 */
@ToadletPath("unbookmark.html")
class UnbookmarkPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		SoneTemplatePage(webInterface, loaders, templateRenderer, pageTitleKey = "Page.Unbookmark.Title") {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		when {
			soneRequest.isGET && (soneRequest.parameters["post"] == "allNotLoaded") -> {
				soneRequest.core.bookmarkedPosts
						.filterNot(Post::isLoaded)
						.forEach(soneRequest.core::unbookmarkPost)
				redirectTo("bookmarks.html")
			}
			soneRequest.isPOST -> {
				soneRequest.parameters["post", 36]
						?.let(soneRequest.core::getPost)
						?.also(soneRequest.core::unbookmarkPost)
				redirectTo(soneRequest.parameters["returnPage", 256])
			}
		}
	}

}
