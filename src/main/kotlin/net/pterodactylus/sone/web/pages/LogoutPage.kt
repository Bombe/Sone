package net.pterodactylus.sone.web.pages

import freenet.clients.http.ToadletContext
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Logs a user out.
 */
class LogoutPage @Inject constructor(template: Template, webInterface: WebInterface):
		LoggedInPage("logout.html", template, "Page.Logout.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext) {
		setCurrentSone(freenetRequest.toadletContext, null)
		throw RedirectException("index.html")
	}

	override fun isEnabled(toadletContext: ToadletContext): Boolean =
			if (webInterface.core.preferences.requireFullAccess && !toadletContext.isAllowedFullAccess) {
				false
			} else
				getCurrentSone(toadletContext) != null && webInterface.core.localSones.size != 1

}
