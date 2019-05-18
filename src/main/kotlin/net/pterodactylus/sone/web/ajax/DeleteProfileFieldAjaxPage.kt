package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import javax.inject.Inject

/**
 * AJAX page that lets the user delete a profile field.
 */
@ToadletPath("deleteProfileField.ajax")
class DeleteProfileFieldAjaxPage @Inject constructor(webInterface: WebInterface) : LoggedInJsonPage(webInterface) {

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
