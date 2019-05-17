package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Page that lets the user distrust another Sone. This will assign a
 * configurable (negative) amount of trust to an identity.
 *
 * @see net.pterodactylus.sone.core.Core#distrustSone(Sone, Sone)
 */
class DistrustPage @Inject constructor(template: Template, webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer):
		LoggedInPage("distrust.html", template, "Page.Distrust.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			soneRequest.core.getSone(soneRequest.httpRequest.getPartAsStringFailsafe("sone", 44))
					?.run { soneRequest.core.distrustSone(currentSone, this) }
			throw RedirectException(soneRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256))
		}
	}

}
