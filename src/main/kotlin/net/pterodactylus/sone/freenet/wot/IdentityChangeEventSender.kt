/*
 * Sone - IdentityChangeEventSender.java - Copyright © 2013–2019 David Roden
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
import net.pterodactylus.sone.freenet.wot.event.*

/**
 * Detects changes in [Identity]s trusted by multiple [OwnIdentity]s.
 *
 * @see IdentityChangeDetector
 */
class IdentityChangeEventSender(private val eventBus: EventBus, private val oldIdentities: Map<OwnIdentity, Collection<Identity>>) {

	fun detectChanges(identities: Map<OwnIdentity, Collection<Identity>>) {
		val identityChangeDetector = IdentityChangeDetector(oldIdentities.keys)
		identityChangeDetector.onNewIdentity = addNewOwnIdentityAndItsTrustedIdentities(identities)
		identityChangeDetector.onRemovedIdentity = removeOwnIdentityAndItsTrustedIdentities(oldIdentities)
		identityChangeDetector.onUnchangedIdentity = detectChangesInTrustedIdentities(identities, oldIdentities)
		identityChangeDetector.detectChanges(identities.keys)
	}

	private fun addNewOwnIdentityAndItsTrustedIdentities(newIdentities: Map<OwnIdentity, Collection<Identity>>) =
			{ identity: Identity ->
				eventBus.post(OwnIdentityAddedEvent(identity as OwnIdentity))
				newIdentities[identity]
						?.map { IdentityAddedEvent(identity, it) }
						?.forEach(eventBus::post) ?: Unit
			}

	private fun removeOwnIdentityAndItsTrustedIdentities(oldIdentities: Map<OwnIdentity, Collection<Identity>>) =
			{ identity: Identity ->
				eventBus.post(OwnIdentityRemovedEvent(identity as OwnIdentity))
				oldIdentities[identity]
						?.map { IdentityRemovedEvent(identity, it) }
						?.forEach(eventBus::post) ?: Unit
			}

	private fun detectChangesInTrustedIdentities(newIdentities: Map<OwnIdentity, Collection<Identity>>, oldIdentities: Map<OwnIdentity, Collection<Identity>>) =
			{ ownIdentity: Identity ->
				val identityChangeDetector = IdentityChangeDetector(oldIdentities[ownIdentity as OwnIdentity]!!)
				identityChangeDetector.onNewIdentity = { eventBus.post(IdentityAddedEvent(ownIdentity, it)) }
				identityChangeDetector.onRemovedIdentity = { eventBus.post(IdentityRemovedEvent(ownIdentity, it)) }
				identityChangeDetector.onChangedIdentity = { eventBus.post(IdentityUpdatedEvent(ownIdentity, it)) }
				identityChangeDetector.detectChanges(newIdentities[ownIdentity]!!)
			}

}
