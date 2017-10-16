package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Lets the user delete a post they made.
 */
class DeletePostPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("deletePost.html", template, "Page.DeletePost.Title", webInterface, true) {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			val post = webInterface.core.getPost(freenetRequest.httpRequest.getPartAsStringFailsafe("post", 36)) ?: throw RedirectException("noPermission.html")
			val returnPage = freenetRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256)
			if (!post.sone.isLocal) {
				throw RedirectException("noPermission.html")
			}
			if (freenetRequest.httpRequest.isPartSet("confirmDelete")) {
				webInterface.core.deletePost(post)
				throw RedirectException(returnPage)
			} else if (freenetRequest.httpRequest.isPartSet("abortDelete")) {
				throw RedirectException(returnPage)
			}
			templateContext["post"] = post
			templateContext["returnPage"] = returnPage
			return
		}
		templateContext["post"] = webInterface.core.getPost(freenetRequest.httpRequest.getParam("post")) ?: throw RedirectException("noPermission.html")
		templateContext["returnPage"] = freenetRequest.httpRequest.getParam("returnPage")
	}

}
