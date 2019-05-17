package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.text.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * This page lets the user post a reply to a post.
 */
@TemplatePath("/templates/createReply.html")
class CreateReplyPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer):
		LoggedInPage("createReply.html", "Page.CreateReply.Title", webInterface, loaders, templateRenderer) {

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
