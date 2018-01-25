package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.data.Profile.Field
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import javax.inject.Inject

/**
 * AJAX page that lets the user move a profile field up or down.
 *
 * @see net.pterodactylus.sone.data.Profile#moveFieldUp(Field)
 * @see net.pterodactylus.sone.data.Profile#moveFieldDown(Field)
 */
class MoveProfileFieldAjaxPage @Inject constructor(webInterface: WebInterface) :
		LoggedInJsonPage("moveProfileField.ajax", webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			currentSone.profile.let { profile ->
				request.parameters["field"]
						?.let(profile::getFieldById)
						?.let { processField(currentSone, profile, it, request.parameters["direction"]) }
						?: createErrorJsonObject("invalid-field-id")
			}

	private fun processField(currentSone: Sone, profile: Profile, field: Field, direction: String?) =
			try {
				when (direction) {
					"up" -> profile.moveFieldUp(field)
					"down" -> profile.moveFieldDown(field)
					else -> null
				}?.let {
					currentSone.profile = profile
					core.touchConfiguration()
					createSuccessJsonObject()
				} ?: createErrorJsonObject("invalid-direction")
			} catch (e: IllegalArgumentException) {
				createErrorJsonObject("not-possible")
			}

}
