package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Profile.DuplicateField
import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * This page lets the user edit her profile.
 */
class EditProfilePage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("editProfile.html", template, "Page.EditProfile.Title", webInterface, true) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		request.currentSone!!.profile.let { profile ->
			templateContext["firstName"] = profile.firstName
			templateContext["middleName"] = profile.middleName
			templateContext["lastName"] = profile.lastName
			templateContext["birthDay"] = profile.birthDay
			templateContext["birthMonth"] = profile.birthMonth
			templateContext["birthYear"] = profile.birthYear
			templateContext["avatarId"] = profile.avatar
			templateContext["fields"] = profile.fields
			if (request.isPOST) {
				if (request.httpRequest.getPartAsStringFailsafe("save-profile", 4) == "true") {
					profile.firstName = request.httpRequest.getPartAsStringFailsafe("first-name", 256).trim()
					profile.middleName = request.httpRequest.getPartAsStringFailsafe("middle-name", 256).trim()
					profile.lastName = request.httpRequest.getPartAsStringFailsafe("last-name", 256).trim()
					profile.birthDay = request.httpRequest.getPartAsStringFailsafe("birth-day", 256).trim().toIntOrNull()
					profile.birthMonth = request.httpRequest.getPartAsStringFailsafe("birth-month", 256).trim().toIntOrNull()
					profile.birthYear = request.httpRequest.getPartAsStringFailsafe("birth-year", 256).trim().toIntOrNull()
					profile.setAvatar(webInterface.core.getImage(request.httpRequest.getPartAsStringFailsafe("avatarId", 256).trim(), false))
					profile.fields.forEach { field ->
						field.value = TextFilter.filter(request.httpRequest.getHeader("Host"), request.httpRequest.getPartAsStringFailsafe("field-${field.id}", 400).trim())
					}
					webInterface.core.touchConfiguration()
					throw RedirectException("editProfile.html")
				} else if (request.httpRequest.getPartAsStringFailsafe("add-field", 4) == "true") {
					val fieldName = request.httpRequest.getPartAsStringFailsafe("field-name", 100)
					try {
						profile.addField(fieldName)
						request.currentSone!!.profile = profile
						webInterface.core.touchConfiguration()
						throw RedirectException("editProfile.html#profile-fields")
					} catch (e: DuplicateField) {
						templateContext["fieldName"] = fieldName
						templateContext["duplicateFieldName"] = true
					}
				} else profile.fields.forEach { field ->
					if (request.httpRequest.getPartAsStringFailsafe("delete-field-${field.id}", 4) == "true") {
						throw RedirectException("deleteProfileField.html?field=${field.id}")
					} else if (request.httpRequest.getPartAsStringFailsafe("edit-field-${field.id}", 4) == "true") {
						throw RedirectException("editProfileField.html?field=${field.id}")
					} else if (request.httpRequest.getPartAsStringFailsafe("move-down-field-${field.id}", 4) == "true") {
						profile.moveFieldDown(field)
						request.currentSone!!.profile = profile
						throw RedirectException("editProfile.html#profile-fields")
					} else if (request.httpRequest.getPartAsStringFailsafe("move-up-field-${field.id}", 4) == "true") {
						profile.moveFieldUp(field)
						request.currentSone!!.profile = profile
						throw RedirectException("editProfile.html#profile-fields")
					}
				}
			}
		}
	}

	private val FreenetRequest.currentSone get() = sessionProvider.getCurrentSone(toadletContext)

}
