package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import java.util.logging.*
import javax.inject.*

/**
 * The “create Sone” page lets the user create a new Sone.
 */
@MenuName("CreateSone")
@TemplatePath("/templates/createSone.html")
@ToadletPath("createSone.html")
class CreateSonePage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		SoneTemplatePage(webInterface, loaders, templateRenderer, pageTitleKey = "Page.CreateSone.Title") {

	private val logger = Logger.getLogger(CreateSonePage::class.java.name)

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		templateContext["sones"] = soneRequest.core.localSones.sortedWith(niceNameComparator)
		templateContext["identitiesWithoutSone"] = soneRequest.core.identityManager.allOwnIdentities.filterNot { "Sone" in it.contexts }.sortedBy { "${it.nickname}@${it.id}".toLowerCase() }
		if (soneRequest.isPOST) {
			val identity = soneRequest.httpRequest.getPartAsStringFailsafe("identity", 43)
			soneRequest.core.identityManager.allOwnIdentities.firstOrNull { it.id == identity }?.let { ownIdentity ->
				val sone = soneRequest.core.createSone(ownIdentity)
				if (sone == null) {
					logger.log(Level.SEVERE, "Could not create Sone for OwnIdentity: $ownIdentity")
				}
				setCurrentSone(soneRequest.toadletContext, sone)
				redirectTo("index.html")
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
