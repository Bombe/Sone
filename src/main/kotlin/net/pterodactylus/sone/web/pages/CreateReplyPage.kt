package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * This page lets the user post a reply to a post.
 */
class CreateReplyPage @Inject constructor(template: Template, webInterface: WebInterface, loaders: Loaders):
		LoggedInPage("createReply.html", template, "Page.CreateReply.Title", webInterface, loaders) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		val postId = soneRequest.httpRequest.getPartAsStringFailsafe("post", 36).apply { templateContext["postId"] = this }
		val text = soneRequest.httpRequest.getPartAsStringFailsafe("text", 65536).trim().apply { templateContext["text"] = this }
		val returnPage = soneRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256).apply { templateContext["returnPage"] = this }
		if (soneRequest.isPOST) {
			if (text == "") {
				templateContext["errorTextEmpty"] = true
				return
			}
			val post = soneRequest.core.getPost(postId) ?: throw RedirectException("noPermission.html")
			val sender = soneRequest.core.getLocalSone(soneRequest.httpRequest.getPartAsStringFailsafe("sender", 43)) ?: currentSone
			soneRequest.core.createReply(sender, post, TextFilter.filter(soneRequest.httpRequest.getHeader("Host"), text))
			throw RedirectException(returnPage)
		}
	}

}
