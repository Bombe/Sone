package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * AJAX page that lets the user delete a profile field.
 */
class DeleteProfileFieldAjaxPage(webInterface: WebInterface) : LoggedInJsonPage("deleteProfileField.ajax", webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			currentSone.profile.let { profile ->
				request.parameters["field"]
						?.let(profile::getFieldById)
						?.let { field ->
							createSuccessJsonObject().also {
								profile.removeField(field)
								currentSone.profile = profile
								core.touchConfiguration()
							}
						} ?: createErrorJsonObject("invalid-field-id")
			}

}
