/*
 * Sone - FreenetRequest.kt - Copyright © 2011–2020 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.web.page

import freenet.clients.http.*
import freenet.clients.http.SessionManager.*
import freenet.support.api.*
import net.pterodactylus.sone.freenet.*
import net.pterodactylus.util.web.*
import java.net.*
import java.util.UUID.*

open class FreenetRequest(uri: URI, method: Method,
		val httpRequest: HTTPRequest,
		val toadletContext: ToadletContext,
		val sessionManager: SessionManager
) : Request(uri, method) {

	val session: Session
		get() =
			sessionManager.useSession(toadletContext)
					?: sessionManager.createSession(randomUUID().toString(), toadletContext)

	val existingSession: Session? get() = sessionManager.useSession(toadletContext)

}
