package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Page that lets the user bookmark a post.
 */
@ToadletPath("bookmark.html")
class BookmarkPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer)
	: SoneTemplatePage("bookmark.html", webInterface, loaders, templateRenderer, pageTitleKey = "Page.Bookmark.Title") {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			val returnPage = soneRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256)
			val postId = soneRequest.httpRequest.getPartAsStringFailsafe("post", 36)
			soneRequest.core.getPost(postId)?.let {
				soneRequest.core.bookmarkPost(it)
			}
			throw RedirectException(returnPage)
		}
	}

}
