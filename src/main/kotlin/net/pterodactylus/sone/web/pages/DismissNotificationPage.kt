package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user dismiss a notification.
 */
class DismissNotificationPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("dismissNotification.html", template, "Page.DismissNotification.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		val returnPage = freenetRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256)
		val notificationId = freenetRequest.httpRequest.getPartAsStringFailsafe("notification", 36)
		webInterface.getNotification(notificationId).orNull()?.takeIf { it.isDismissable }?.dismiss()
		throw RedirectException(returnPage)
	}

}
