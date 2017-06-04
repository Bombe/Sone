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

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		if (request.isPOST) {
			when (request.parameters["type"]) {
				"post" -> getCurrentSone(request.toadletContext)!!.removeLikedPostId(request.parameters["post"]!!)
				"reply" -> getCurrentSone(request.toadletContext)!!.removeLikedReplyId(request.parameters["reply"]!!)
			}
			throw RedirectException(request.parameters["returnPage", 256])
		}
	}

}
