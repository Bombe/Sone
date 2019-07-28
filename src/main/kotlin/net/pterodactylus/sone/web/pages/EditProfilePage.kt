package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.data.Profile.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.text.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * This page lets the user edit her profile.
 */
@MenuName("EditProfile")
@TemplatePath("/templates/editProfile.html")
@ToadletPath("editProfile.html")
class EditProfilePage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("Page.EditProfile.Title", webInterface, loaders, templateRenderer) {

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
					redirectTo("editProfile.html")
				} else if (soneRequest.httpRequest.getPartAsStringFailsafe("add-field", 4) == "true") {
					val fieldName = soneRequest.httpRequest.getPartAsStringFailsafe("field-name", 100)
					try {
						profile.addField(fieldName)
						currentSone.profile = profile
						soneRequest.core.touchConfiguration()
						redirectTo("editProfile.html#profile-fields")
					} catch (e: DuplicateField) {
						templateContext["fieldName"] = fieldName
						templateContext["duplicateFieldName"] = true
					}
				} else profile.fields.forEach { field ->
					if (soneRequest.httpRequest.getPartAsStringFailsafe("delete-field-${field.id}", 4) == "true") {
						redirectTo("deleteProfileField.html?field=${field.id}")
					} else if (soneRequest.httpRequest.getPartAsStringFailsafe("edit-field-${field.id}", 4) == "true") {
						redirectTo("editProfileField.html?field=${field.id}")
					} else if (soneRequest.httpRequest.getPartAsStringFailsafe("move-down-field-${field.id}", 4) == "true") {
						profile.moveFieldDown(field)
						currentSone.profile = profile
						redirectTo("editProfile.html#profile-fields")
					} else if (soneRequest.httpRequest.getPartAsStringFailsafe("move-up-field-${field.id}", 4) == "true") {
						profile.moveFieldUp(field)
						currentSone.profile = profile
						redirectTo("editProfile.html#profile-fields")
					}
				}
			}
		}
	}

}
