package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * Returns the translation for a given key as JSON object.
 */
class GetTranslationAjaxPage(webInterface: WebInterface) : JsonPage("getTranslation.ajax", webInterface) {

	override fun needsFormPassword() = false
	override fun requiresLogin() = false

	override fun createJsonObject(request: FreenetRequest) =
			createSuccessJsonObject()
					.put("value", webInterface.l10n.getString(request.parameters["key"]))

}
