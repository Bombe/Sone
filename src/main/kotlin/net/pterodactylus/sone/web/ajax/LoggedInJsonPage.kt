package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * Base JSON page for all pages that require the user to be logged in.
 */
open class LoggedInJsonPage(path: String, webInterface: WebInterface) : JsonPage(path, webInterface) {

	final override fun requiresLogin() = true

	final override fun createJsonObject(request: FreenetRequest) =
			createJsonObject(getCurrentSone(request.toadletContext)!!, request)

	open protected fun createJsonObject(currentSone: Sone, request: FreenetRequest): JsonReturnObject =
			createErrorJsonObject("not-implemented")

}
