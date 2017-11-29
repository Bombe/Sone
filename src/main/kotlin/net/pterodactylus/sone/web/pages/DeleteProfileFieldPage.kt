package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user confirm the deletion of a profile field.
 */
class DeleteProfileFieldPage(template: Template, webInterface: WebInterface):
		LoggedInPage("deleteProfileField.html", template, "Page.DeleteProfileField.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			val field = currentSone.profile.getFieldById(freenetRequest.httpRequest.getPartAsStringFailsafe("field", 36)) ?: throw RedirectException("invalid.html")
			if (freenetRequest.httpRequest.getPartAsStringFailsafe("confirm", 4) == "true") {
				currentSone.profile = currentSone.profile.apply { removeField(field) }
			}
			throw RedirectException("editProfile.html#profile-fields")
		}
		val field = currentSone.profile.getFieldById(freenetRequest.httpRequest.getParam("field")) ?: throw RedirectException("invalid.html")
		templateContext["field"] = field
	}

}
