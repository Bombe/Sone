package net.pterodactylus.sone.web

import freenet.clients.http.ToadletContext
import net.pterodactylus.sone.data.Sone

/**
 * Provides access to the currently logged-in [Sone].
 */
interface SessionProvider {

	fun getCurrentSone(toadletContext: ToadletContext, createSession: Boolean = true): Sone?
	fun setCurrentSone(toadletContext: ToadletContext, sone: Sone?)

}
