package net.pterodactylus.sone.web.pages

import freenet.clients.http.ToadletContext
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.pages.SoneTemplatePage
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * The login page lets the user log in.
 */
class LoginPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("login.html", template, "Page.Login.Title", webInterface) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		if (request.isPOST) {
			val soneId = request.httpRequest.getPartAsStringFailsafe("sone-id", 43)
			webInterface.core.getLocalSone(soneId)?.let { sone ->
				setCurrentSone(request.toadletContext, sone)
				val target = if (request.httpRequest.isParameterSet("target")) request.httpRequest.getPartAsStringFailsafe("target", 256) else "index.html"
				throw RedirectException(target)
			}
		}
		templateContext["sones"] = webInterface.core.localSones.sortedWith(Sone.NICE_NAME_COMPARATOR)
		templateContext["identitiesWithoutSone"] = webInterface.core.identityManager.allOwnIdentities.filterNot { "Sone" in it.contexts }.sortedBy { "${it.nickname}@${it.id}" }
	}

	override public fun getRedirectTarget(request: FreenetRequest) =
			getCurrentSone(request.toadletContext)?.let { "index.html" }

	override fun isEnabled(toadletContext: ToadletContext) = when {
		webInterface.core.preferences.isRequireFullAccess && !toadletContext.isAllowedFullAccess -> false
		else -> getCurrentSone(toadletContext, false) == null
	}

}
