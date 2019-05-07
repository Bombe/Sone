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
 * Page that lets the user unlike a [net.pterodactylus.sone.data.Post] or [net.pterodactylus.sone.data.Reply].
 */
class UnlikePage @Inject constructor(template: Template, webInterface: WebInterface):
		LoggedInPage("unlike.html", template, "Page.Unlike.Title", webInterface) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			when (soneRequest.parameters["type"]) {
				"post" -> currentSone.removeLikedPostId(soneRequest.parameters["post"]!!)
				"reply" -> currentSone.removeLikedReplyId(soneRequest.parameters["reply"]!!)
			}
			throw RedirectException(soneRequest.parameters["returnPage", 256])
		}
	}

}
