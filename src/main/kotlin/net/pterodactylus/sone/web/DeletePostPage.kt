package net.pterodactylus.sone.web

import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Lets the user delete a post they made.
 */
class DeletePostPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("deletePost.html", template, "Page.DeletePost.Title", webInterface, true) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		val post = webInterface.core.getPost(request.httpRequest.getPartAsStringFailsafe("post", 36)).orNull() ?: throw RedirectException("noPermission.html")
		val returnPage = request.httpRequest.getPartAsStringFailsafe("returnPage", 256)
		if (request.isPOST) {
			if (!post.sone.isLocal) {
				throw RedirectException("noPermission.html")
			}
			if (request.httpRequest.isPartSet("confirmDelete")) {
				webInterface.core.deletePost(post)
				throw RedirectException(returnPage)
			} else if (request.httpRequest.isPartSet("abortDelete")) {
				throw RedirectException(returnPage)
			}
		}
		templateContext["post"] = post
		templateContext["returnPage"] = returnPage
	}

}
