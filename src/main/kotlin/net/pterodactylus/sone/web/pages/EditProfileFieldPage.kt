package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user edit the name of a profile field.
 */
class EditProfileFieldPage(template: Template, webInterface: WebInterface) :
		LoggedInPage("editProfileField.html", template, "Page.EditProfileField.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext) {
		currentSone.profile.let { profile ->
			if (freenetRequest.isPOST) {
				if (freenetRequest.httpRequest.getPartAsStringFailsafe("cancel", 4) == "true") {
					throw RedirectException("editProfile.html#profile-fields")
				}
				val field = profile.getFieldById(freenetRequest.httpRequest.getPartAsStringFailsafe("field", 36)) ?: throw RedirectException("invalid.html")
				freenetRequest.httpRequest.getPartAsStringFailsafe("name", 256).let { name ->
					try {
						if (name != field.name) {
							field.name = name
							currentSone.profile = profile
						}
						throw RedirectException("editProfile.html#profile-fields")
					} catch (e: IllegalArgumentException) {
						templateContext["duplicateFieldName"] = true
						return
					}
				}
			}
			templateContext["field"] = profile.getFieldById(freenetRequest.httpRequest.getParam("field")) ?: throw RedirectException("invalid.html")
		}
	}

}
