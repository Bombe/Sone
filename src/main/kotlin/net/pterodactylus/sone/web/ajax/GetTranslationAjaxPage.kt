package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import javax.inject.Inject

/**
 * Returns the translation for a given key as JSON object.
 */
@ToadletPath("getTranslation.ajax")
class GetTranslationAjaxPage @Inject constructor(webInterface: WebInterface) : JsonPage(webInterface) {

	override val needsFormPassword = false
	override val requiresLogin = false

	override fun createJsonObject(request: FreenetRequest) =
			createSuccessJsonObject()
					.put("value", webInterface.translation.translate(request.parameters["key"] ?: ""))

}
