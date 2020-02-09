package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Page that lets the user edit the name of a profile field.
 */
@TemplatePath("/templates/editProfileField.html")
@ToadletPath("editProfileField.html")
class EditProfileFieldPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("Page.EditProfileField.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		currentSone.profile.let { profile ->
			if (soneRequest.isPOST) {
				if (soneRequest.httpRequest.getPartAsStringFailsafe("cancel", 4) == "true") {
					redirectTo("editProfile.html#profile-fields")
				}
				val field = profile.getFieldById(soneRequest.httpRequest.getPartAsStringFailsafe("field", 36)) ?: redirectTo("invalid.html")
				soneRequest.httpRequest.getPartAsStringFailsafe("name", 256).let { name ->
					try {
						if (name != field.name) {
							field.name = name
							currentSone.profile = profile
						}
						redirectTo("editProfile.html#profile-fields")
					} catch (e: IllegalArgumentException) {
						templateContext["duplicateFieldName"] = true
						return
					}
				}
			}
			templateContext["field"] = profile.getFieldById(soneRequest.httpRequest.getParam("field")) ?: redirectTo("invalid.html")
		}
	}

}
