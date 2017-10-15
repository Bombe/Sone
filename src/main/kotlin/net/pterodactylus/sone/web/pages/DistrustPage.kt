package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user distrust another Sone. This will assign a
 * configurable (negative) amount of trust to an identity.
 *
 * @see net.pterodactylus.sone.core.Core#distrustSone(Sone, Sone)
 */
class DistrustPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("distrust.html", template, "Page.Distrust.Title", webInterface, true) {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			webInterface.core.getSone(freenetRequest.httpRequest.getPartAsStringFailsafe("sone", 44))
					?.run { webInterface.core.distrustSone(getCurrentSone(freenetRequest.toadletContext), this) }
			throw RedirectException(freenetRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256))
		}
	}

}
