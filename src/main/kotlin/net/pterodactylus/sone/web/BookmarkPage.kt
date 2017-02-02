package net.pterodactylus.sone.web

import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import net.pterodactylus.util.web.Method.POST
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Page that lets the user bookmark a post.
 */
@Singleton
class BookmarkPage @Inject constructor(template: Template, webInterface: WebInterface)
	: SoneTemplatePage("bookmark.html", template, "Page.Bookmark.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		if (freenetRequest.method == POST) {
			val returnPage = freenetRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256)
			val postId = freenetRequest.httpRequest.getPartAsStringFailsafe("post", 36)
			webInterface.core.getPost(postId).orNull()?.let {
				webInterface.core.bookmarkPost(it)
			}
			throw RedirectException(returnPage)
		}
	}

}
