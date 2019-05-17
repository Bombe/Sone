package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.utils.asOptional
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * This page lets the user create a new [Post].
 */
@TemplatePath("/templates/createPost.html")
class CreatePostPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer):
		LoggedInPage("createPost.html", "Page.CreatePost.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		val returnPage = soneRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256)
		templateContext["returnPage"] = returnPage
		if (soneRequest.isPOST) {
			val text = soneRequest.httpRequest.getPartAsStringFailsafe("text", 65536).trim()
			if (text == "") {
				templateContext["errorTextEmpty"] = true
				return
			}
			val sender = soneRequest.core.getLocalSone(soneRequest.httpRequest.getPartAsStringFailsafe("sender", 43)) ?: currentSone
			val recipient = soneRequest.core.getSone(soneRequest.httpRequest.getPartAsStringFailsafe("recipient", 43))
			soneRequest.core.createPost(sender, recipient.asOptional(), TextFilter.filter(soneRequest.httpRequest.getHeader("Host"), text))
			throw RedirectException(returnPage)
		}
	}

}
