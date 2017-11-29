package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * This page lets the user unfollow another Sone.
 */
class UnfollowSonePage(template: Template, webInterface: WebInterface) :
		LoggedInPage("unfollowSone.html", template, "Page.UnfollowSone.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			freenetRequest.parameters["sone"]!!.split(Regex("[ ,]+"))
					.forEach { webInterface.core.unfollowSone(currentSone, it) }
			throw RedirectException(freenetRequest.parameters["returnPage", 256])
		}
	}

}
