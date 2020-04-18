/*
 * Sone - IdentityManagerImpl.kt - Copyright © 2010–2020 David Roden
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

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.google.inject.Inject
import com.google.inject.Singleton
import net.pterodactylus.sone.core.event.StrictFilteringActivatedEvent
import net.pterodactylus.sone.core.event.StrictFilteringDeactivatedEvent
import net.pterodactylus.util.service.AbstractService
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.Logger.getLogger

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
	private val strictFiltering = AtomicBoolean(false)

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
				val currentIdentities = identityLoader.loadAllIdentities().applyStrictFiltering()

				val identityChangeEventSender = IdentityChangeEventSender(eventBus, oldIdentities)
				identityChangeEventSender.detectChanges(currentIdentities)

				oldIdentities = currentIdentities

				synchronized(currentOwnIdentities) {
					currentOwnIdentities.clear()
					currentOwnIdentities.addAll(currentIdentities.keys)
				}
			} catch (wote1: WebOfTrustException) {
				logger.log(Level.WARNING, "WoT has disappeared!", wote1)
			} catch (e: Exception) {
				logger.log(Level.SEVERE, "Uncaught exception in IdentityManager thread!", e)
			}

			/* wait a minute before checking again. */
			sleep(SECONDS.toMillis(60))
		}
	}

	private fun Map<OwnIdentity, Set<Identity>>.applyStrictFiltering() =
			if (strictFiltering.get()) {
				val identitiesWithTrust = values.flatten()
						.groupBy { it.id }
						.mapValues { (_, identities) ->
							identities.reduce { accIdentity, identity ->
								identity.trust.forEach { (ownIdentity: OwnIdentity?, trust: Trust?) ->
									accIdentity.setTrust(ownIdentity, trust)
								}
								accIdentity
							}
						}

				mapValues { (_, trustedIdentities) ->
					trustedIdentities.filter { trustedIdentity ->
						identitiesWithTrust[trustedIdentity.id]!!.trust.all { it.value.hasZeroOrPositiveTrust() }
					}
				}
			} else {
				this
			}

	@Subscribe
	fun strictFilteringActivated(event: StrictFilteringActivatedEvent) {
		strictFiltering.set(true)
	}

	@Subscribe
	fun strictFilteringDeactivated(event: StrictFilteringDeactivatedEvent) {
		strictFiltering.set(false)
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

private fun Trust.hasZeroOrPositiveTrust() =
		if (explicit == null) {
			implicit == null || implicit >= 0
		} else {
			explicit >= 0
		}
