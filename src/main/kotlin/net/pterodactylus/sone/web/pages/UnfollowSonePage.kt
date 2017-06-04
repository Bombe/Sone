package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import java.util.regex.Pattern

/**
 * This page lets the user unfollow another Sone.
 */
class UnfollowSonePage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("unfollowSone.html", template, "Page.UnfollowSone.Title", webInterface, true) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		if (request.isPOST) {
			getCurrentSone(request.toadletContext)!!.also { currentSone ->
				request.parameters["sone"]!!.split(Regex("[ ,]+"))
						.forEach { webInterface.core.unfollowSone(currentSone, it) }
			}
			throw RedirectException(request.parameters["returnPage", 256])
		}
	}

}
