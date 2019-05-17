package net.pterodactylus.sone.web.pages

import freenet.clients.http.ToadletContext
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Logs a user out.
 */
@MenuName("Logout")
class LogoutPage @Inject constructor(template: Template, webInterface: WebInterface):
		LoggedInPage("logout.html", template, "Page.Logout.Title", webInterface) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		setCurrentSone(soneRequest.toadletContext, null)
		throw RedirectException("index.html")
	}

	override fun isEnabled(soneRequest: SoneRequest): Boolean =
			if (soneRequest.core.preferences.requireFullAccess && !soneRequest.toadletContext.isAllowedFullAccess) {
				false
			} else
				getCurrentSone(soneRequest.toadletContext) != null && soneRequest.core.localSones.size != 1

}
