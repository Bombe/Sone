package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.pages.SoneTemplatePage
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

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		if (request.isPOST) {
			val sone = webInterface.core.getSone(request.httpRequest.getPartAsStringFailsafe("sone", 44)).orNull()
			sone?.run { webInterface.core.distrustSone(getCurrentSone(request.toadletContext), this) }
			throw RedirectException(request.httpRequest.getPartAsStringFailsafe("returnPage", 256))
		}
	}

}
