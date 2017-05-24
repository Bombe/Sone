package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user bookmark a post.
 */
class BookmarkPage(template: Template, webInterface: WebInterface)
	: SoneTemplatePage("bookmark.html", template, "Page.Bookmark.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			val returnPage = freenetRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256)
			val postId = freenetRequest.httpRequest.getPartAsStringFailsafe("post", 36)
			webInterface.core.getPost(postId).orNull()?.let {
				webInterface.core.bookmarkPost(it)
			}
			throw RedirectException(returnPage)
		}
	}

}
