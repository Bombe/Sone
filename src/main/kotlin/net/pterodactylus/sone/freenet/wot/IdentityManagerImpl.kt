/*
 * Sone - IdentityManagerImpl.java - Copyright © 2010–2019 David Roden
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

package net.pterodactylus.sone.freenet.wot

import com.google.common.eventbus.*
import com.google.inject.*
import net.pterodactylus.util.service.*
import java.util.concurrent.TimeUnit.*
import java.util.logging.*
import java.util.logging.Logger.*

/**
 * The identity manager takes care of loading and storing identities, their
 * contexts, and properties. It does so in a way that does not expose errors via
 * exceptions but it only logs them and tries to return sensible defaults.
 *
 *
 * It is also responsible for polling identities from the Web of Trust plugin
 * and sending events to the [EventBus] when [Identity]s and
 * [OwnIdentity]s are discovered or disappearing.
 */
@Singleton
class IdentityManagerImpl @Inject constructor(
		private val eventBus: EventBus,
		private val webOfTrustConnector: WebOfTrustConnector,
		private val identityLoader: IdentityLoader
) : AbstractService("Sone Identity Manager", false), IdentityManager {

	private val currentOwnIdentities = mutableSetOf<OwnIdentity>()

	override val isConnected: Boolean
		get() = notThrowing { webOfTrustConnector.ping() }

	override val allOwnIdentities: Set<OwnIdentity>
		get() = synchronized(currentOwnIdentities) {
			currentOwnIdentities.toSet()
		}

	override fun serviceRun() {
		var oldIdentities = mapOf<OwnIdentity, Collection<Identity>>()

		while (!shouldStop()) {
			try {
				val currentIdentities = identityLoader.loadIdentities()

				val identityChangeEventSender = IdentityChangeEventSender(eventBus, oldIdentities)
				identityChangeEventSender.detectChanges(currentIdentities)

				oldIdentities = currentIdentities

				synchronized(currentOwnIdentities) {
					currentOwnIdentities.clear()
					currentOwnIdentities.addAll(currentIdentities.keys)
				}
			} catch (wote1: WebOfTrustException) {
				logger.log(Level.WARNING, "WoT has disappeared!", wote1)
			}

			/* wait a minute before checking again. */
			sleep(SECONDS.toMillis(60))
		}
	}

}

private val logger: Logger = getLogger(IdentityManagerImpl::class.java.name)

private fun notThrowing(action: () -> Unit): Boolean =
		try {
			action()
			true
		} catch (e: Exception) {
			false
		}
