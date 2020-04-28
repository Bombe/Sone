package net.pterodactylus.sone.web.page

import freenet.clients.http.*
import freenet.support.api.*
import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.util.web.*
import java.net.*

class SoneRequest(uri: URI, method: Method, httpRequest: HTTPRequest, toadletContext: ToadletContext,
				  val core: Core,
				  val webInterface: WebInterface
) : FreenetRequest(uri, method, httpRequest, toadletContext)

fun FreenetRequest.toSoneRequest(core: Core, webInterface: WebInterface) =
		SoneRequest(uri, method, httpRequest, toadletContext, core, webInterface)
