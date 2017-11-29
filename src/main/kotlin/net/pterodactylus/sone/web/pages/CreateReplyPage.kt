package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
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
		LoggedInPage("createReply.html", template, "Page.CreateReply.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext) {
		val postId = freenetRequest.httpRequest.getPartAsStringFailsafe("post", 36).apply { templateContext["postId"] = this }
		val text = freenetRequest.httpRequest.getPartAsStringFailsafe("text", 65536).trim().apply { templateContext["text"] = this }
		val returnPage = freenetRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256).apply { templateContext["returnPage"] = this }
		if (freenetRequest.isPOST) {
			if (text == "") {
				templateContext["errorTextEmpty"] = true
				return
			}
			val post = webInterface.core.getPost(postId) ?: throw RedirectException("noPermission.html")
			val sender = webInterface.core.getLocalSone(freenetRequest.httpRequest.getPartAsStringFailsafe("sender", 43)) ?: currentSone
			webInterface.core.createReply(sender, post, TextFilter.filter(freenetRequest.httpRequest.getHeader("Host"), text))
			throw RedirectException(returnPage)
		}
	}

}
