package net.pterodactylus.sone.web.pages

import freenet.clients.http.ToadletContext
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.emptyToNull
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * The login page lets the user log in.
 */
class LoginPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("login.html", template, "Page.Login.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			val soneId = freenetRequest.httpRequest.getPartAsStringFailsafe("sone-id", 43)
			webInterface.core.getLocalSone(soneId)?.let { sone ->
				setCurrentSone(freenetRequest.toadletContext, sone)
				val target = freenetRequest.httpRequest.getParam("target").emptyToNull ?: "index.html"
				throw RedirectException(target)
			}
		}
		templateContext["sones"] = webInterface.core.localSones.sortedWith(Sone.NICE_NAME_COMPARATOR)
		templateContext["identitiesWithoutSone"] = webInterface.core.identityManager.allOwnIdentities.filterNot { "Sone" in it.contexts }.sortedBy { "${it.nickname}@${it.id}" }
	}

	override public fun getRedirectTarget(freenetRequest: FreenetRequest) =
			getCurrentSone(freenetRequest.toadletContext)?.let { "index.html" }

	override fun isEnabled(toadletContext: ToadletContext) = when {
		webInterface.core.preferences.isRequireFullAccess && !toadletContext.isAllowedFullAccess -> false
		else -> getCurrentSone(toadletContext, false) == null
	}

}
