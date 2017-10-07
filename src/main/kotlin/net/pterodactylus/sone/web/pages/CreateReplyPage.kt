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

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		val postId = freenetRequest.httpRequest.getPartAsStringFailsafe("post", 36).apply { templateContext["postId"] = this }
		val text = freenetRequest.httpRequest.getPartAsStringFailsafe("text", 65536).trim().apply { templateContext["text"] = this }
		val returnPage = freenetRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256).apply { templateContext["returnPage"] = this }
		if (freenetRequest.isPOST) {
			if (text == "") {
				templateContext["errorTextEmpty"] = true
				return
			}
			val post = webInterface.core.getPost(postId).orNull() ?: throw RedirectException("noPermission.html")
			val sender = webInterface.core.getLocalSone(freenetRequest.httpRequest.getPartAsStringFailsafe("sender", 43)) ?: getCurrentSone(freenetRequest.toadletContext)
			webInterface.core.createReply(sender, post, TextFilter.filter(freenetRequest.httpRequest.getHeader("Host"), text))
			throw RedirectException(returnPage)
		}
	}

}
