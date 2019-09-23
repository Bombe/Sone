/*
 * Sone - IdentityChangeDetectorTest.java - Copyright © 2013–2019 David Roden
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

import net.pterodactylus.sone.freenet.wot.Identities.createIdentity
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty

import org.junit.Before
import org.junit.Test

/**
 * Unit test for [IdentityChangeDetector].
 */
class IdentityChangeDetectorTest {

	private val identityChangeDetector = IdentityChangeDetector(createOldIdentities())
	private val newIdentities = mutableListOf<Identity>()
	private val removedIdentities = mutableListOf<Identity>()
	private val changedIdentities = mutableListOf<Identity>()
	private val unchangedIdentities = mutableListOf<Identity>()

	@Before
	fun setup() {
		identityChangeDetector.onNewIdentity { identity -> newIdentities.add(identity) }
		identityChangeDetector.onRemovedIdentity { identity -> removedIdentities.add(identity) }
		identityChangeDetector.onChangedIdentity { identity -> changedIdentities.add(identity) }
		identityChangeDetector.onUnchangedIdentity { identity -> unchangedIdentities.add(identity) }
	}

	@Test
	fun `no differences are detected when sending the old identities again`() {
		identityChangeDetector.detectChanges(createOldIdentities())
		assertThat<Collection<Identity>>(newIdentities, empty())
		assertThat<Collection<Identity>>(removedIdentities, empty())
		assertThat<Collection<Identity>>(changedIdentities, empty())
		assertThat<Collection<Identity>>(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity2(), createIdentity3()))
	}

	@Test
	fun `detect that an identity was removed`() {
		identityChangeDetector.detectChanges(listOf(createIdentity1(), createIdentity3()))
		assertThat<Collection<Identity>>(newIdentities, empty())
		assertThat<Collection<Identity>>(removedIdentities, containsInAnyOrder(createIdentity2()))
		assertThat<Collection<Identity>>(changedIdentities, empty())
		assertThat<Collection<Identity>>(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity3()))
	}

	@Test
	fun `detect that an identity was added`() {
		identityChangeDetector.detectChanges(listOf(createIdentity1(), createIdentity2(), createIdentity3(), createIdentity4()))
		assertThat<Collection<Identity>>(newIdentities, containsInAnyOrder(createIdentity4()))
		assertThat<Collection<Identity>>(removedIdentities, empty())
		assertThat<Collection<Identity>>(changedIdentities, empty())
		assertThat<Collection<Identity>>(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity2(), createIdentity3()))
	}

	@Test
	fun `detect that a context was removed`() {
		val identity2 = createIdentity2()
		identity2.removeContext("Context C")
		identityChangeDetector.detectChanges(listOf(createIdentity1(), identity2, createIdentity3()))
		assertThat<Collection<Identity>>(newIdentities, empty())
		assertThat<Collection<Identity>>(removedIdentities, empty())
		assertThat<Collection<Identity>>(changedIdentities, containsInAnyOrder(identity2))
		assertThat<Collection<Identity>>(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity3()))
	}

	@Test
	fun `detect that a context was added`() {
		val identity2 = createIdentity2()
		identity2.addContext("Context C1")
		identityChangeDetector.detectChanges(listOf(createIdentity1(), identity2, createIdentity3()))
		assertThat<Collection<Identity>>(newIdentities, empty())
		assertThat<Collection<Identity>>(removedIdentities, empty())
		assertThat<Collection<Identity>>(changedIdentities, containsInAnyOrder(identity2))
		assertThat<Collection<Identity>>(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity3()))
	}

	@Test
	fun `detect that a property was removed`() {
		val identity1 = createIdentity1()
		identity1.removeProperty("Key A")
		identityChangeDetector.detectChanges(listOf(identity1, createIdentity2(), createIdentity3()))
		assertThat<Collection<Identity>>(newIdentities, empty())
		assertThat<Collection<Identity>>(removedIdentities, empty())
		assertThat<Collection<Identity>>(changedIdentities, containsInAnyOrder(identity1))
		assertThat<Collection<Identity>>(unchangedIdentities, containsInAnyOrder(createIdentity2(), createIdentity3()))
	}

	@Test
	fun `detect that a property was added`() {
		val identity3 = createIdentity3()
		identity3.setProperty("Key A", "Value A")
		identityChangeDetector.detectChanges(listOf(createIdentity1(), createIdentity2(), identity3))
		assertThat<Collection<Identity>>(newIdentities, empty())
		assertThat<Collection<Identity>>(removedIdentities, empty())
		assertThat<Collection<Identity>>(changedIdentities, containsInAnyOrder(identity3))
		assertThat<Collection<Identity>>(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity2()))
	}

	@Test
	fun `detect that a property was changed`() {
		val identity3 = createIdentity3()
		identity3.setProperty("Key E", "Value F")
		identityChangeDetector.detectChanges(listOf(createIdentity1(), createIdentity2(), identity3))
		assertThat<Collection<Identity>>(newIdentities, empty())
		assertThat<Collection<Identity>>(removedIdentities, empty())
		assertThat<Collection<Identity>>(changedIdentities, containsInAnyOrder(identity3))
		assertThat<Collection<Identity>>(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity2()))
	}

	@Test
	fun `no removed identities are detected without an identity processor`() {
		identityChangeDetector.onRemovedIdentity(null)
		identityChangeDetector.detectChanges(listOf(createIdentity1(), createIdentity3()))
		assertThat(removedIdentities, empty())
	}

	@Test
	fun `no added identities are detected without an identity processor`() {
		identityChangeDetector.onNewIdentity(null)
		identityChangeDetector.detectChanges(listOf(createIdentity1(), createIdentity2(), createIdentity3(), createIdentity4()))
		assertThat(newIdentities, empty())
	}

	private fun createOldIdentities(): Collection<Identity> {
		return listOf(createIdentity1(), createIdentity2(), createIdentity3())
	}

	private fun createIdentity1(): Identity {
		return createIdentity("Test1", listOf("Context A", "Context B"), mapOf("Key A" to "Value A", "Key B" to "Value B"))
	}

	private fun createIdentity2(): Identity {
		return createIdentity("Test2", listOf("Context C", "Context D"), mapOf("Key C" to "Value C", "Key D" to "Value D"))
	}

	private fun createIdentity3(): Identity {
		return createIdentity("Test3", listOf("Context E", "Context F"), mapOf("Key E" to "Value E", "Key F" to "Value F"))
	}

	private fun createIdentity4(): Identity {
		return createIdentity("Test4", listOf("Context G", "Context H"), mapOf("Key G" to "Value G", "Key H" to "Value H"))
	}

}
