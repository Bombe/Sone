package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user unlike a [net.pterodactylus.sone.data.Post] or [net.pterodactylus.sone.data.Reply].
 */
class UnlikePage(template: Template, webInterface: WebInterface):
		LoggedInPage("unlike.html", template, "Page.Unlike.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			when (freenetRequest.parameters["type"]) {
				"post" -> currentSone.removeLikedPostId(freenetRequest.parameters["post"]!!)
				"reply" -> currentSone.removeLikedReplyId(freenetRequest.parameters["reply"]!!)
			}
			throw RedirectException(freenetRequest.parameters["returnPage", 256])
		}
	}

}
