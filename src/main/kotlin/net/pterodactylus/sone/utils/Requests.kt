package net.pterodactylus.sone.utils

import freenet.support.api.HTTPRequest
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import net.pterodactylus.util.web.Request

val Request.isGET get() = this.method == GET
val Request.isPOST get() = this.method == POST
val HTTPRequest.isGET get() = method == "GET"
val HTTPRequest.isPOST get() = method == "POST"

val FreenetRequest.parameters get() = Parameters(httpRequest)
val HTTPRequest.parameters get() = Parameters(this)

class Parameters(private val request: HTTPRequest) {
	operator fun get(name: String, maxLength: Int = 1048576) = when {
		request.isGET -> request.getParam(name)
		request.isPOST -> request.getPartAsStringFailsafe(name, maxLength)
		else -> null
	}

	operator fun contains(name: String) = when {
		request.isGET -> request.isParameterSet(name)
		request.isPOST -> request.isPartSet(name)
		else -> false
	}
}
