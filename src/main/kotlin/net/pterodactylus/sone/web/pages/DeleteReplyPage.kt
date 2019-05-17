package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * This page lets the user delete a reply.
 */
class DeleteReplyPage @Inject constructor(template: Template, webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer):
		LoggedInPage("deleteReply.html", template, "Page.DeleteReply.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			val replyId = soneRequest.httpRequest.getPartAsStringFailsafe("reply", 36)
			val reply = soneRequest.core.getPostReply(replyId) ?: throw RedirectException("noPermission.html")
			if (!reply.sone.isLocal) {
				throw RedirectException("noPermission.html")
			}
			val returnPage = soneRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256)
			if (soneRequest.httpRequest.isPartSet("confirmDelete")) {
				soneRequest.core.deleteReply(reply)
				throw RedirectException(returnPage)
			}
			if (soneRequest.httpRequest.isPartSet("abortDelete")) {
				throw RedirectException(returnPage)
			}
			templateContext["reply"] = replyId
			templateContext["returnPage"] = returnPage
			return
		}
		templateContext["reply"] = soneRequest.httpRequest.getParam("reply")
		templateContext["returnPage"] = soneRequest.httpRequest.getParam("returnPage")
	}

}
