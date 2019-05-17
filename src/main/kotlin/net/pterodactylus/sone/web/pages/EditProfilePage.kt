package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Profile.DuplicateField
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * This page lets the user edit her profile.
 */
@MenuName("EditProfile")
class EditProfilePage @Inject constructor(template: Template, webInterface: WebInterface, loaders: Loaders) :
		LoggedInPage("editProfile.html", template, "Page.EditProfile.Title", webInterface, loaders) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		currentSone.profile.let { profile ->
			templateContext["firstName"] = profile.firstName
			templateContext["middleName"] = profile.middleName
			templateContext["lastName"] = profile.lastName
			templateContext["birthDay"] = profile.birthDay
			templateContext["birthMonth"] = profile.birthMonth
			templateContext["birthYear"] = profile.birthYear
			templateContext["avatarId"] = profile.avatar
			templateContext["fields"] = profile.fields
			if (soneRequest.isPOST) {
				if (soneRequest.httpRequest.getPartAsStringFailsafe("save-profile", 4) == "true") {
					profile.firstName = soneRequest.httpRequest.getPartAsStringFailsafe("first-name", 256).trim()
					profile.middleName = soneRequest.httpRequest.getPartAsStringFailsafe("middle-name", 256).trim()
					profile.lastName = soneRequest.httpRequest.getPartAsStringFailsafe("last-name", 256).trim()
					profile.birthDay = soneRequest.httpRequest.getPartAsStringFailsafe("birth-day", 256).trim().toIntOrNull()
					profile.birthMonth = soneRequest.httpRequest.getPartAsStringFailsafe("birth-month", 256).trim().toIntOrNull()
					profile.birthYear = soneRequest.httpRequest.getPartAsStringFailsafe("birth-year", 256).trim().toIntOrNull()
					profile.setAvatar(soneRequest.core.getImage(soneRequest.httpRequest.getPartAsStringFailsafe("avatarId", 256).trim(), false))
					profile.fields.forEach { field ->
						field.value = TextFilter.filter(soneRequest.httpRequest.getHeader("Host"), soneRequest.httpRequest.getPartAsStringFailsafe("field-${field.id}", 400).trim())
					}
					currentSone.profile = profile
					soneRequest.core.touchConfiguration()
					throw RedirectException("editProfile.html")
				} else if (soneRequest.httpRequest.getPartAsStringFailsafe("add-field", 4) == "true") {
					val fieldName = soneRequest.httpRequest.getPartAsStringFailsafe("field-name", 100)
					try {
						profile.addField(fieldName)
						currentSone.profile = profile
						soneRequest.core.touchConfiguration()
						throw RedirectException("editProfile.html#profile-fields")
					} catch (e: DuplicateField) {
						templateContext["fieldName"] = fieldName
						templateContext["duplicateFieldName"] = true
					}
				} else profile.fields.forEach { field ->
					if (soneRequest.httpRequest.getPartAsStringFailsafe("delete-field-${field.id}", 4) == "true") {
						throw RedirectException("deleteProfileField.html?field=${field.id}")
					} else if (soneRequest.httpRequest.getPartAsStringFailsafe("edit-field-${field.id}", 4) == "true") {
						throw RedirectException("editProfileField.html?field=${field.id}")
					} else if (soneRequest.httpRequest.getPartAsStringFailsafe("move-down-field-${field.id}", 4) == "true") {
						profile.moveFieldDown(field)
						currentSone.profile = profile
						throw RedirectException("editProfile.html#profile-fields")
					} else if (soneRequest.httpRequest.getPartAsStringFailsafe("move-up-field-${field.id}", 4) == "true") {
						profile.moveFieldUp(field)
						currentSone.profile = profile
						throw RedirectException("editProfile.html#profile-fields")
					}
				}
			}
		}
	}

}
