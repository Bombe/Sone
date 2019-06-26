package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * The login page lets the user log in.
 */
@MenuName("Login")
@TemplatePath("/templates/login.html")
@ToadletPath("login.html")
class LoginPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		SoneTemplatePage(webInterface, loaders, templateRenderer, pageTitleKey = "Page.Login.Title") {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			val soneId = soneRequest.httpRequest.getPartAsStringFailsafe("sone-id", 43)
			soneRequest.core.getLocalSone(soneId)?.let { sone ->
				setCurrentSone(soneRequest.toadletContext, sone)
				val target = soneRequest.httpRequest.getParam("target").emptyToNull ?: "index.html"
				throw RedirectException(target)
			}
		}
		templateContext["sones"] = soneRequest.core.localSones.sortedWith(Sone.NICE_NAME_COMPARATOR)
		templateContext["identitiesWithoutSone"] = soneRequest.core.identityManager.allOwnIdentities.filterNot { "Sone" in it.contexts }.sortedBy { "${it.nickname}@${it.id}" }
	}

	override fun getRedirectTarget(request: FreenetRequest) =
			getCurrentSone(request.toadletContext)?.let { "index.html" }

	override fun isEnabled(soneRequest: SoneRequest) = when {
		soneRequest.core.preferences.requireFullAccess && !soneRequest.toadletContext.isAllowedFullAccess -> false
		else -> getCurrentSone(soneRequest.toadletContext, false) == null
	}

}
