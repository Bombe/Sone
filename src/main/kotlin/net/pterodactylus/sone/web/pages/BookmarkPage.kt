package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Page that lets the user bookmark a post.
 */
class BookmarkPage @Inject constructor(template: Template, webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer)
	: SoneTemplatePage("bookmark.html", webInterface, loaders, template, templateRenderer, pageTitleKey = "Page.Bookmark.Title") {

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
