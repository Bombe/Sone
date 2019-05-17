package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * This page lets the user delete a reply.
 */
@TemplatePath("/templates/deleteReply.html")
@ToadletPath("deleteReply.html")
class DeleteReplyPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("deleteReply.html", "Page.DeleteReply.Title", webInterface, loaders, templateRenderer) {

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
