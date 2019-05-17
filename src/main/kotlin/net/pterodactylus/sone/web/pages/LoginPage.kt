package net.pterodactylus.sone.web.pages

import freenet.clients.http.ToadletContext
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.emptyToNull
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * The login page lets the user log in.
 */
@MenuName("Login")
@TemplatePath("/templates/login.html")
class LoginPage @Inject constructor(template: Template, webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer):
		SoneTemplatePage("login.html", webInterface, loaders, template, templateRenderer, pageTitleKey = "Page.Login.Title") {

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

	override fun getRedirectTarget(freenetRequest: FreenetRequest) =
			getCurrentSone(freenetRequest.toadletContext)?.let { "index.html" }

	override fun isEnabled(soneRequest: SoneRequest) = when {
		soneRequest.core.preferences.requireFullAccess && !soneRequest.toadletContext.isAllowedFullAccess -> false
		else -> getCurrentSone(soneRequest.toadletContext, false) == null
	}

}
