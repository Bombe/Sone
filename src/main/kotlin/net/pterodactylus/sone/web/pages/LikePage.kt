package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Page that lets the user like [net.pterodactylus.sone.data.Post]s and [net.pterodactylus.sone.data.Reply]s.
 */
class LikePage @Inject constructor(template: Template, webInterface: WebInterface) :
		LoggedInPage("like.html", template, "Page.Like.Title", webInterface) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			soneRequest.parameters["type", 16]?.also { type ->
				when (type) {
					"post" -> currentSone.addLikedPostId(soneRequest.parameters["post", 36]!!)
					"reply" -> currentSone.addLikedReplyId(soneRequest.parameters["reply", 36]!!)
				}
			}
			throw RedirectException(soneRequest.parameters["returnPage", 256]!!)
		}
	}

}
