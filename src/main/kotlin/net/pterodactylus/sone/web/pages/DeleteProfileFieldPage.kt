package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.pages.SoneTemplatePage
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user confirm the deletion of a profile field.
 */
class DeleteProfileFieldPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("deleteProfileField.html", template, "Page.DeleteProfileField.Title", webInterface, true) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		val currentSone = getCurrentSone(request.toadletContext)!!
		if (request.isPOST) {
			val field = currentSone.profile.getFieldById(request.httpRequest.getPartAsStringFailsafe("field", 36)) ?: throw RedirectException("invalid.html")
			if (request.httpRequest.getPartAsStringFailsafe("confirm", 4) == "true") {
				currentSone.profile = currentSone.profile.apply { removeField(field) }
			}
			throw RedirectException("editProfile.html#profile-fields")
		}
		val field = currentSone.profile.getFieldById(request.httpRequest.getParam("field")) ?: throw RedirectException("invalid.html")
		templateContext["field"] = field
	}

}
