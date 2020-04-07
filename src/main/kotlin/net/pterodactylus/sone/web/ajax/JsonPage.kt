package net.pterodactylus.sone.web.ajax

import com.fasterxml.jackson.databind.ObjectMapper
import freenet.clients.http.ToadletContext
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.SessionProvider
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.web.Page
import net.pterodactylus.util.web.Response
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * A JSON page is a specialized [Page] that will always return a JSON
 * object to the browser, e.g. for use with AJAX or other scripting frameworks.
 */
abstract class JsonPage(protected val webInterface: WebInterface) : Page<FreenetRequest> {

	private val objectMapper = ObjectMapper()
	private val sessionProvider: SessionProvider = webInterface
	protected val core = webInterface.core

	override fun getPath() = toadletPath
	override fun isPrefixPage() = false

	open val needsFormPassword = true
	open val requiresLogin = true

	protected fun createSuccessJsonObject() = JsonReturnObject(true)
	protected fun createErrorJsonObject(error: String) =
			JsonErrorReturnObject(error)

	protected fun getCurrentSone(toadletContext: ToadletContext, createSession: Boolean = true) =
			sessionProvider.getCurrentSone(toadletContext)

	override fun handleRequest(request: FreenetRequest, response: Response): Response {
		if (core.preferences.requireFullAccess && !request.toadletContext.isAllowedFullAccess) {
			return response.setStatusCode(403).setStatusText("Forbidden").setContentType("application/json").write(createErrorJsonObject("auth-required").asJsonString())
		}
		if (needsFormPassword && request.parameters["formPassword"] != webInterface.formPassword) {
			return response.setStatusCode(403).setStatusText("Forbidden").setContentType("application/json").write(createErrorJsonObject("auth-required").asJsonString())
		}
		if (requiresLogin && (sessionProvider.getCurrentSone(request.toadletContext) == null)) {
			return response.setStatusCode(403).setStatusText("Forbidden").setContentType("application/json").write(createErrorJsonObject("auth-required").asJsonString())
		}
		return try {
			response.setStatusCode(200).setStatusText("OK").setContentType("application/json").write(createJsonObject(request).asJsonString())
		} catch (e: Exception) {
			response.setStatusCode(500).setStatusText(e.message).setContentType("text/plain").write(e.dumpStackTrace())
		}
	}

	abstract fun createJsonObject(request: FreenetRequest): JsonReturnObject

	private fun JsonReturnObject.asJsonString(): String = objectMapper.writeValueAsString(this)

	private fun Throwable.dumpStackTrace(): String = ByteArrayOutputStream().use {
		PrintStream(it, true, "UTF-8").use {
			this.printStackTrace(it)
		}
		it.toString("UTF-8")
	}

}
