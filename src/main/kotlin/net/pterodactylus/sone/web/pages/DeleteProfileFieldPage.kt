package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Page that lets the user confirm the deletion of a profile field.
 */
@TemplatePath("/templates/deleteProfileField.html")
@ToadletPath("deleteProfileField.html")
class DeleteProfileFieldPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("Page.DeleteProfileField.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			val field = currentSone.profile.getFieldById(soneRequest.httpRequest.getPartAsStringFailsafe("field", 36)) ?: throw RedirectException("invalid.html")
			if (soneRequest.httpRequest.getPartAsStringFailsafe("confirm", 4) == "true") {
				currentSone.profile = currentSone.profile.apply { removeField(field) }
			}
			throw RedirectException("editProfile.html#profile-fields")
		}
		val field = currentSone.profile.getFieldById(soneRequest.httpRequest.getParam("field")) ?: throw RedirectException("invalid.html")
		templateContext["field"] = field
	}

}
