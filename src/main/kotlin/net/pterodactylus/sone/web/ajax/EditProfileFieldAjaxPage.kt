package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.utils.ifFalse
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * AJAX page that lets the user rename a profile field.
 */
class EditProfileFieldAjaxPage(webInterface: WebInterface) : JsonPage("editProfileField.ajax", webInterface) {

	override fun createJsonObject(request: FreenetRequest) =
			getCurrentSone(request.toadletContext).profile.let { profile ->
				request.parameters["field"]!!
						.let(profile::getFieldById)
						?.let { field ->
							request.parameters["name"]!!.trim().let { newName ->
								newName.isBlank().ifFalse {
									try {
										field.name = newName
										createSuccessJsonObject().also {
											getCurrentSone(request.toadletContext).profile = profile
										}
									} catch (_: IllegalArgumentException) {
										createErrorJsonObject("duplicate-field-name")
									}
								}
							} ?: createErrorJsonObject("invalid-parameter-name")
						} ?: createErrorJsonObject("invalid-field-id")
			}

}
