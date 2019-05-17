package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Logs a user out.
 */
@MenuName("Logout")
@ToadletPath("logout.html")
class LogoutPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("Page.Logout.Title", webInterface, loaders, templateRenderer) {

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
