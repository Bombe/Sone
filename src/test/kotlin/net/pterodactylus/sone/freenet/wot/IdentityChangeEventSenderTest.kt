/*
 * Sone - IdentityChangeEventSenderTest.java - Copyright © 2013–2019 David Roden
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
import net.pterodactylus.sone.test.*
import org.junit.*
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.verify

/**
 * Unit test for [IdentityChangeEventSender].
 */
class IdentityChangeEventSenderTest {

	private val eventBus = mock<EventBus>()
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
		identityChangeEventSender.detectChanges(newIdentities)
		verify(eventBus).post(eq(OwnIdentityRemovedEvent(ownIdentities[0])))
		verify(eventBus).post(eq(IdentityRemovedEvent(ownIdentities[0], identities[0])))
		verify(eventBus).post(eq(IdentityRemovedEvent(ownIdentities[0], identities[1])))
		verify(eventBus).post(eq(OwnIdentityAddedEvent(ownIdentities[2])))
		verify(eventBus).post(eq(IdentityAddedEvent(ownIdentities[2], identities[1])))
		verify(eventBus).post(eq(IdentityAddedEvent(ownIdentities[2], identities[2])))
		verify(eventBus).post(eq(IdentityRemovedEvent(ownIdentities[1], identities[0])))
		verify(eventBus).post(eq(IdentityAddedEvent(ownIdentities[1], identities[2])))
		verify(eventBus).post(eq(IdentityUpdatedEvent(ownIdentities[1], identities[1])))
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
