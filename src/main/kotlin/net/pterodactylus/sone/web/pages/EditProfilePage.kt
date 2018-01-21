package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Profile.DuplicateField
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * This page lets the user edit her profile.
 */
class EditProfilePage @Inject constructor(template: Template, webInterface: WebInterface) :
		LoggedInPage("editProfile.html", template, "Page.EditProfile.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext) {
		currentSone.profile.let { profile ->
			templateContext["firstName"] = profile.firstName
			templateContext["middleName"] = profile.middleName
			templateContext["lastName"] = profile.lastName
			templateContext["birthDay"] = profile.birthDay
			templateContext["birthMonth"] = profile.birthMonth
			templateContext["birthYear"] = profile.birthYear
			templateContext["avatarId"] = profile.avatar
			templateContext["fields"] = profile.fields
			if (freenetRequest.isPOST) {
				if (freenetRequest.httpRequest.getPartAsStringFailsafe("save-profile", 4) == "true") {
					profile.firstName = freenetRequest.httpRequest.getPartAsStringFailsafe("first-name", 256).trim()
					profile.middleName = freenetRequest.httpRequest.getPartAsStringFailsafe("middle-name", 256).trim()
					profile.lastName = freenetRequest.httpRequest.getPartAsStringFailsafe("last-name", 256).trim()
					profile.birthDay = freenetRequest.httpRequest.getPartAsStringFailsafe("birth-day", 256).trim().toIntOrNull()
					profile.birthMonth = freenetRequest.httpRequest.getPartAsStringFailsafe("birth-month", 256).trim().toIntOrNull()
					profile.birthYear = freenetRequest.httpRequest.getPartAsStringFailsafe("birth-year", 256).trim().toIntOrNull()
					profile.setAvatar(webInterface.core.getImage(freenetRequest.httpRequest.getPartAsStringFailsafe("avatarId", 256).trim(), false))
					profile.fields.forEach { field ->
						field.value = TextFilter.filter(freenetRequest.httpRequest.getHeader("Host"), freenetRequest.httpRequest.getPartAsStringFailsafe("field-${field.id}", 400).trim())
					}
					currentSone.profile = profile
					webInterface.core.touchConfiguration()
					throw RedirectException("editProfile.html")
				} else if (freenetRequest.httpRequest.getPartAsStringFailsafe("add-field", 4) == "true") {
					val fieldName = freenetRequest.httpRequest.getPartAsStringFailsafe("field-name", 100)
					try {
						profile.addField(fieldName)
						currentSone.profile = profile
						webInterface.core.touchConfiguration()
						throw RedirectException("editProfile.html#profile-fields")
					} catch (e: DuplicateField) {
						templateContext["fieldName"] = fieldName
						templateContext["duplicateFieldName"] = true
					}
				} else profile.fields.forEach { field ->
					if (freenetRequest.httpRequest.getPartAsStringFailsafe("delete-field-${field.id}", 4) == "true") {
						throw RedirectException("deleteProfileField.html?field=${field.id}")
					} else if (freenetRequest.httpRequest.getPartAsStringFailsafe("edit-field-${field.id}", 4) == "true") {
						throw RedirectException("editProfileField.html?field=${field.id}")
					} else if (freenetRequest.httpRequest.getPartAsStringFailsafe("move-down-field-${field.id}", 4) == "true") {
						profile.moveFieldDown(field)
						currentSone.profile = profile
						throw RedirectException("editProfile.html#profile-fields")
					} else if (freenetRequest.httpRequest.getPartAsStringFailsafe("move-up-field-${field.id}", 4) == "true") {
						profile.moveFieldUp(field)
						currentSone.profile = profile
						throw RedirectException("editProfile.html#profile-fields")
					}
				}
			}
		}
	}

}
