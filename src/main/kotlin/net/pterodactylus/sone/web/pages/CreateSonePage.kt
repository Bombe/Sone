package net.pterodactylus.sone.web.pages

import freenet.clients.http.ToadletContext
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject

/**
 * The “create Sone” page lets the user create a new Sone.
 */
@MenuName("CreateSone")
class CreateSonePage @Inject constructor(template: Template, webInterface: WebInterface, loaders: Loaders):
		SoneTemplatePage("createSone.html", webInterface, loaders, template = template, pageTitleKey = "Page.CreateSone.Title") {

	private val logger = Logger.getLogger(CreateSonePage::class.java.name)

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		templateContext["sones"] = soneRequest.core.localSones.sortedWith(Sone.NICE_NAME_COMPARATOR)
		templateContext["identitiesWithoutSone"] = soneRequest.core.identityManager.allOwnIdentities.filterNot { "Sone" in it.contexts }.sortedBy { "${it.nickname}@${it.id}".toLowerCase() }
		if (soneRequest.isPOST) {
			val identity = soneRequest.httpRequest.getPartAsStringFailsafe("identity", 43)
			soneRequest.core.identityManager.allOwnIdentities.firstOrNull { it.id == identity }?.let { ownIdentity ->
				val sone = soneRequest.core.createSone(ownIdentity)
				if (sone == null) {
					logger.log(Level.SEVERE, "Could not create Sone for OwnIdentity: $ownIdentity")
				}
				setCurrentSone(soneRequest.toadletContext, sone)
				throw RedirectException("index.html")
			}
			templateContext["errorNoIdentity"] = true
		}
	}

	override fun isEnabled(soneRequest: SoneRequest) =
			if (soneRequest.core.preferences.requireFullAccess && !soneRequest.toadletContext.isAllowedFullAccess) {
				false
			} else {
				(getCurrentSone(soneRequest.toadletContext) == null) || (soneRequest.core.localSones.size == 1)
			}

}
