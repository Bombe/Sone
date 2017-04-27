package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.pages.SoneTemplatePage
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * This page lets the user delete a reply.
 */
class DeleteReplyPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("deleteReply.html", template, "Page.DeleteReply.Title", webInterface, true) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		if (request.isPOST) {
			val replyId = request.httpRequest.getPartAsStringFailsafe("reply", 36)
			val reply = webInterface.core.getPostReply(replyId).orNull() ?: throw RedirectException("noPermission.html")
			if (!reply.sone.isLocal) {
				throw RedirectException("noPermission.html")
			}
			val returnPage = request.httpRequest.getPartAsStringFailsafe("returnPage", 256)
			if (request.httpRequest.isPartSet("confirmDelete")) {
				webInterface.core.deleteReply(reply)
				throw RedirectException(returnPage)
			}
			if (request.httpRequest.isPartSet("abortDelete")) {
				throw RedirectException(returnPage)
			}
			templateContext["reply"] = replyId
			templateContext["returnPage"] = returnPage
			return
		}
		templateContext["reply"] = request.httpRequest.getParam("reply")
		templateContext["returnPage"] = request.httpRequest.getParam("returnPage")
	}

}
