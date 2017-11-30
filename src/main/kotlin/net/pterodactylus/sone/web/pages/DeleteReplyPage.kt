package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * This page lets the user delete a reply.
 */
class DeleteReplyPage(template: Template, webInterface: WebInterface):
		LoggedInPage("deleteReply.html", template, "Page.DeleteReply.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			val replyId = freenetRequest.httpRequest.getPartAsStringFailsafe("reply", 36)
			val reply = webInterface.core.getPostReply(replyId) ?: throw RedirectException("noPermission.html")
			if (!reply.sone.isLocal) {
				throw RedirectException("noPermission.html")
			}
			val returnPage = freenetRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256)
			if (freenetRequest.httpRequest.isPartSet("confirmDelete")) {
				webInterface.core.deleteReply(reply)
				throw RedirectException(returnPage)
			}
			if (freenetRequest.httpRequest.isPartSet("abortDelete")) {
				throw RedirectException(returnPage)
			}
			templateContext["reply"] = replyId
			templateContext["returnPage"] = returnPage
			return
		}
		templateContext["reply"] = freenetRequest.httpRequest.getParam("reply")
		templateContext["returnPage"] = freenetRequest.httpRequest.getParam("returnPage")
	}

}
