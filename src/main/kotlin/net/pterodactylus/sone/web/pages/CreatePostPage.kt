package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.utils.asOptional
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * This page lets the user create a new [Post].
 */
class CreatePostPage @Inject constructor(template: Template, webInterface: WebInterface):
		LoggedInPage("createPost.html", template, "Page.CreatePost.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext) {
		val returnPage = freenetRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256)
		templateContext["returnPage"] = returnPage
		if (freenetRequest.isPOST) {
			val text = freenetRequest.httpRequest.getPartAsStringFailsafe("text", 65536).trim()
			if (text == "") {
				templateContext["errorTextEmpty"] = true
				return
			}
			val sender = webInterface.core.getLocalSone(freenetRequest.httpRequest.getPartAsStringFailsafe("sender", 43)) ?: currentSone
			val recipient = webInterface.core.getSone(freenetRequest.httpRequest.getPartAsStringFailsafe("recipient", 43))
			webInterface.core.createPost(sender, recipient.asOptional(), TextFilter.filter(freenetRequest.httpRequest.getHeader("Host"), text))
			throw RedirectException(returnPage)
		}
	}

}
