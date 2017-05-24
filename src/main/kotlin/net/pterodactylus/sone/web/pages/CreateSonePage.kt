package net.pterodactylus.sone.web.pages

import freenet.clients.http.ToadletContext
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import java.util.logging.Level
import java.util.logging.Logger

/**
 * The “create Sone” page lets the user create a new Sone.
 */
class CreateSonePage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("createSone.html", template, "Page.CreateSone.Title", webInterface, false) {

	private val logger = Logger.getLogger(CreateSonePage::class.java.name)

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		templateContext["sones"] = webInterface.core.localSones.sortedWith(Sone.NICE_NAME_COMPARATOR)
		templateContext["identitiesWithoutSone"] = webInterface.core.identityManager.allOwnIdentities.filterNot { "Sone" in it.contexts }.sortedBy { "${it.nickname}@${it.id}".toLowerCase() }
		if (request.isPOST) {
			val identity = request.httpRequest.getPartAsStringFailsafe("identity", 43)
			webInterface.core.identityManager.allOwnIdentities.firstOrNull { it.id == identity }?.let { ownIdentity ->
				val sone = webInterface.core.createSone(ownIdentity)
				if (sone == null) {
					logger.log(Level.SEVERE, "Could not create Sone for OwnIdentity: $ownIdentity")
				}
				setCurrentSone(request.toadletContext, sone)
				throw RedirectException("index.html")
			}
			templateContext["errorNoIdentity"] = true
		}
	}

	override fun isEnabled(toadletContext: ToadletContext) =
			if (webInterface.core.preferences.isRequireFullAccess && !toadletContext.isAllowedFullAccess) {
				false
			} else {
				(getCurrentSone(toadletContext) == null) || (webInterface.core.localSones.size == 1)
			}

}
