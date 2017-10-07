package net.pterodactylus.sone.web.pages

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
		SoneTemplatePage("unlike.html", template, "Page.Unlike.Title", webInterface, true) {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			when (freenetRequest.parameters["type"]) {
				"post" -> getCurrentSone(freenetRequest.toadletContext)!!.removeLikedPostId(freenetRequest.parameters["post"]!!)
				"reply" -> getCurrentSone(freenetRequest.toadletContext)!!.removeLikedReplyId(freenetRequest.parameters["reply"]!!)
			}
			throw RedirectException(freenetRequest.parameters["returnPage", 256])
		}
	}

}
