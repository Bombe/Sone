package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Page that lets the user distrust another Sone. This will assign a
 * configurable (negative) amount of trust to an identity.
 *
 * @see net.pterodactylus.sone.core.Core#distrustSone(Sone, Sone)
 */
class DistrustPage @Inject constructor(template: Template, webInterface: WebInterface):
		LoggedInPage("distrust.html", template, "Page.Distrust.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			webInterface.core.getSone(freenetRequest.httpRequest.getPartAsStringFailsafe("sone", 44))
					?.run { webInterface.core.distrustSone(currentSone, this) }
			throw RedirectException(freenetRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256))
		}
	}

}
