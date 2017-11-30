package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user like [net.pterodactylus.sone.data.Post]s and [net.pterodactylus.sone.data.Reply]s.
 */
class LikePage(template: Template, webInterface: WebInterface) :
		LoggedInPage("like.html", template, "Page.Like.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			freenetRequest.parameters["type", 16]?.also { type ->
				when (type) {
					"post" -> currentSone.addLikedPostId(freenetRequest.parameters["post", 36]!!)
					"reply" -> currentSone.addLikedReplyId(freenetRequest.parameters["reply", 36]!!)
				}
			}
			throw RedirectException(freenetRequest.parameters["returnPage", 256]!!)
		}
	}

}
