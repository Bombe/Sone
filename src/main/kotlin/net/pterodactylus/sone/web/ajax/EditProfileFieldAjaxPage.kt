package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.ifFalse
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import javax.inject.Inject

/**
 * AJAX page that lets the user rename a profile field.
 */
@ToadletPath("editProfileField.ajax")
class EditProfileFieldAjaxPage @Inject constructor(webInterface: WebInterface) : LoggedInJsonPage(webInterface) {

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			currentSone.profile.let { profile ->
				request.parameters["field"]!!
						.let(profile::getFieldById)
						?.let { field ->
							request.parameters["name"]!!.trim().let { newName ->
								newName.isBlank().ifFalse {
									try {
										field.name = newName
										createSuccessJsonObject().also {
											currentSone.profile = profile
										}
									} catch (_: IllegalArgumentException) {
										createErrorJsonObject("duplicate-field-name")
									}
								}
							} ?: createErrorJsonObject("invalid-parameter-name")
						} ?: createErrorJsonObject("invalid-field-id")
			}

}
