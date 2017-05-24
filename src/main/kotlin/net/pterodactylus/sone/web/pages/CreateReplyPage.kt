package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * This page lets the user post a reply to a post.
 */
class CreateReplyPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("createReply.html", template, "Page.CreateReply.Title", webInterface, true) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		val postId = request.httpRequest.getPartAsStringFailsafe("post", 36).apply { templateContext["postId"] = this }
		val text = request.httpRequest.getPartAsStringFailsafe("text", 65536).trim().apply { templateContext["text"] = this }
		val returnPage = request.httpRequest.getPartAsStringFailsafe("returnPage", 256).apply { templateContext["returnPage"] = this }
		if (request.isPOST) {
			if (text == "") {
				templateContext["errorTextEmpty"] = true
				return
			}
			val post = webInterface.core.getPost(postId).orNull() ?: throw RedirectException("noPermission.html")
			val sender = webInterface.core.getLocalSone(request.httpRequest.getPartAsStringFailsafe("sender", 43)) ?: getCurrentSone(request.toadletContext)
			webInterface.core.createReply(sender, post, TextFilter.filter(request.httpRequest.getHeader("Host"), text))
			throw RedirectException(returnPage)
		}
	}

}
