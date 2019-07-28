package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Page that lets the user distrust another Sone. This will assign a
 * configurable (negative) amount of trust to an identity.
 *
 * @see net.pterodactylus.sone.core.Core#distrustSone(Sone, Sone)
 */
@ToadletPath("distrust.html")
class DistrustPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("Page.Distrust.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			soneRequest.core.getSone(soneRequest.httpRequest.getPartAsStringFailsafe("sone", 44))
					?.run { soneRequest.core.distrustSone(currentSone, this) }
			redirectTo(soneRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256))
		}
	}

}
