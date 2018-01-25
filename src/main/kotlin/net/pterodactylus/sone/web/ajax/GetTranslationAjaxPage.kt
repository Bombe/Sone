package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import javax.inject.Inject

/**
 * Returns the translation for a given key as JSON object.
 */
class GetTranslationAjaxPage @Inject constructor(webInterface: WebInterface) :
		JsonPage("getTranslation.ajax", webInterface) {

	override val needsFormPassword = false
	override val requiresLogin = false

	override fun createJsonObject(request: FreenetRequest) =
			createSuccessJsonObject()
					.put("value", webInterface.l10n.getString(request.parameters["key"]))

}
