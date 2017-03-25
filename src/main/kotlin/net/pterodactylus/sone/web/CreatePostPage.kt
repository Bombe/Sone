package net.pterodactylus.sone.web

import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * This page lets the user create a new [Post].
 */
class CreatePostPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("createPost.html", template, "Page.CreatePost.Title", webInterface, true) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		val returnPage = request.httpRequest.getPartAsStringFailsafe("returnPage", 256)
		templateContext["returnPage"] = returnPage
		if (request.isPOST) {
			val text = request.httpRequest.getPartAsStringFailsafe("text", 65536).trim()
			if (text == "") {
				templateContext["errorTextEmpty"] = true
				return
			}
			val sender = webInterface.core.getLocalSone(request.httpRequest.getPartAsStringFailsafe("sender", 43)) ?: getCurrentSone(request.toadletContext)
			val recipient = webInterface.core.getSone(request.httpRequest.getPartAsStringFailsafe("recipient", 43))
			webInterface.core.createPost(sender, recipient, TextFilter.filter(request.httpRequest.getHeader("Host"), text))
			throw RedirectException(returnPage)
		}
	}

}
