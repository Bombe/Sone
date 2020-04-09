/**
 * Sone - FreenetSessionProvider.kt - Copyright © 2020 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.web

import freenet.clients.http.SessionManager
import freenet.clients.http.ToadletContext
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.database.SoneProvider
import java.util.UUID
import javax.inject.Inject

/**
 * [SoneProvider] implementation based on Freenet’s [SessionManager].
 */
class FreenetSessionProvider @Inject constructor(private val soneProvider: SoneProvider, private val sessionManager: SessionManager) : SessionProvider {

	override fun getCurrentSone(toadletContext: ToadletContext): Sone? =
			soneProvider.localSones.singleOrNull()
					?: sessionManager.useSession(toadletContext)
							?.let { it.getAttribute("Sone.CurrentSone") as? String }
							?.let(soneProvider.soneLoader)
							?.takeIf { it.isLocal }

	override fun setCurrentSone(toadletContext: ToadletContext, sone: Sone?) {
		if (sone == null) {
			sessionManager.useSession(toadletContext)
					?.removeAttribute("Sone.CurrentSone")
		} else {
			sessionManager.getOrCreateSession(toadletContext)
					?.setAttribute("Sone.CurrentSone", sone.id)
		}
	}

	private fun SessionManager.getOrCreateSession(toadletContext: ToadletContext) =
			useSession(toadletContext)
					?: createSession(UUID.randomUUID().toString(), toadletContext)

}
