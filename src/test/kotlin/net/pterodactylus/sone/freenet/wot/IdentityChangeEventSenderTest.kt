/*
 * Sone - IdentityChangeEventSenderTest.kt - Copyright © 2013–2020 David Roden
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
import net.pterodactylus.sone.freenet.wot.event.IdentityAddedEvent
import net.pterodactylus.sone.freenet.wot.event.IdentityRemovedEvent
import net.pterodactylus.sone.freenet.wot.event.IdentityUpdatedEvent
import net.pterodactylus.sone.freenet.wot.event.OwnIdentityAddedEvent
import net.pterodactylus.sone.freenet.wot.event.OwnIdentityRemovedEvent
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.Test

/**
 * Unit test for [IdentityChangeEventSender].
 */
class IdentityChangeEventSenderTest {

	private val eventBus = EventBus()
	private val ownIdentities = listOf(
			createOwnIdentity("O1", setOf("Test"), "KeyA" to "ValueA"),
			createOwnIdentity("O2", setOf("Test2"), "KeyB" to "ValueB"),
			createOwnIdentity("O3", setOf("Test3"), "KeyC" to "ValueC")
	)
	private val identities = listOf(
			createIdentity("I1", setOf()),
			createIdentity("I2", setOf()),
			createIdentity("I3", setOf()),
			createIdentity("I2", setOf("Test"))
	)
	private val identityChangeEventSender = IdentityChangeEventSender(eventBus, createOldIdentities())

	@Test
	fun addingAnOwnIdentityIsDetectedAndReportedCorrectly() {
		val newIdentities = createNewIdentities()
		val ownIdentityRemovedEvents = mutableListOf<OwnIdentityRemovedEvent>()
		eventBus.register(object {
			@Subscribe
			fun ownIdentityRemoved(ownIdentityRemovedEvent: OwnIdentityRemovedEvent) {
				ownIdentityRemovedEvents += ownIdentityRemovedEvent
			}
		})
		val identityRemovedEvents = mutableListOf<IdentityRemovedEvent>()
		eventBus.register(object {
			@Subscribe
			fun identityRemovedEvent(identityRemovedEvent: IdentityRemovedEvent) {
				identityRemovedEvents += identityRemovedEvent
			}
		})
		val owIdentityAddedEvents = mutableListOf<OwnIdentityAddedEvent>()
		eventBus.register(object {
			@Subscribe
			fun ownIdentityAdded(ownIdentityAddedEvent: OwnIdentityAddedEvent) {
				owIdentityAddedEvents += ownIdentityAddedEvent
			}
		})
		val identityAddedEvents = mutableListOf<IdentityAddedEvent>()
		eventBus.register(object {
			@Subscribe
			fun identityAdded(identityAddedEvent: IdentityAddedEvent) {
				identityAddedEvents += identityAddedEvent
			}
		})
		val identityUpdatedEvents = mutableListOf<IdentityUpdatedEvent>()
		eventBus.register(object {
			@Subscribe
			fun identityUpdatedEvent(identityUpdatedEvent: IdentityUpdatedEvent) {
				identityUpdatedEvents += identityUpdatedEvent
			}
		})
		identityChangeEventSender.detectChanges(newIdentities)
		assertThat(ownIdentityRemovedEvents, contains(OwnIdentityRemovedEvent(ownIdentities[0])))
		assertThat(identityRemovedEvents, containsInAnyOrder(
				IdentityRemovedEvent(ownIdentities[0], identities[0]),
				IdentityRemovedEvent(ownIdentities[0], identities[1]),
				IdentityRemovedEvent(ownIdentities[1], identities[0])
		))
		assertThat(owIdentityAddedEvents, contains(OwnIdentityAddedEvent(ownIdentities[2])))
		assertThat(identityAddedEvents, containsInAnyOrder(
				IdentityAddedEvent(ownIdentities[2], identities[1]),
				IdentityAddedEvent(ownIdentities[2], identities[2]),
				IdentityAddedEvent(ownIdentities[1], identities[2])
		))
		assertThat(identityUpdatedEvents, contains(IdentityUpdatedEvent(ownIdentities[1], identities[1])))
	}

	private fun createNewIdentities() = mapOf(
			ownIdentities[1] to listOf(identities[3], identities[2]),
			ownIdentities[2] to listOf(identities[1], identities[2])
	)

	private fun createOldIdentities() = mapOf(
			ownIdentities[0] to listOf(identities[0], identities[1]),
			ownIdentities[1] to listOf(identities[0], identities[1])
	)

}
