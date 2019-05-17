package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Page that lets the user dismiss a notification.
 */
class DismissNotificationPage @Inject constructor(template: Template, webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer):
		SoneTemplatePage("dismissNotification.html", webInterface, loaders, template, templateRenderer, pageTitleKey = "Page.DismissNotification.Title") {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		val returnPage = soneRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256)
		val notificationId = soneRequest.httpRequest.getPartAsStringFailsafe("notification", 36)
		soneRequest.webInterface.getNotification(notificationId).orNull()?.takeIf { it.isDismissable }?.dismiss()
		throw RedirectException(returnPage)
	}

}
