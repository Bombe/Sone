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

import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*

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
		identityChangeDetector.onNewIdentity = { identity -> newIdentities.add(identity) }
		identityChangeDetector.onRemovedIdentity = { identity -> removedIdentities.add(identity) }
		identityChangeDetector.onChangedIdentity = { identity -> changedIdentities.add(identity) }
		identityChangeDetector.onUnchangedIdentity = { identity -> unchangedIdentities.add(identity) }
	}

	@Test
	fun `no differences are detected when sending the old identities again`() {
		identityChangeDetector.detectChanges(createOldIdentities())
		assertThat(newIdentities, empty())
		assertThat(removedIdentities, empty())
		assertThat(changedIdentities, empty())
		assertThat(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity2(), createIdentity3()))
	}

	@Test
	fun `detect that an identity was removed`() {
		identityChangeDetector.detectChanges(listOf(createIdentity1(), createIdentity3()))
		assertThat(newIdentities, empty())
		assertThat(removedIdentities, containsInAnyOrder(createIdentity2()))
		assertThat(changedIdentities, empty())
		assertThat(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity3()))
	}

	@Test
	fun `detect that an identity was added`() {
		identityChangeDetector.detectChanges(listOf(createIdentity1(), createIdentity2(), createIdentity3(), createIdentity4()))
		assertThat(newIdentities, containsInAnyOrder(createIdentity4()))
		assertThat(removedIdentities, empty())
		assertThat(changedIdentities, empty())
		assertThat(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity2(), createIdentity3()))
	}

	@Test
	fun `detect that a context was removed`() {
		val identity2 = createIdentity2()
		identity2.removeContext("Context C")
		identityChangeDetector.detectChanges(listOf(createIdentity1(), identity2, createIdentity3()))
		assertThat(newIdentities, empty())
		assertThat(removedIdentities, empty())
		assertThat(changedIdentities, containsInAnyOrder(identity2))
		assertThat(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity3()))
	}

	@Test
	fun `detect that a context was added`() {
		val identity2 = createIdentity2()
		identity2.addContext("Context C1")
		identityChangeDetector.detectChanges(listOf(createIdentity1(), identity2, createIdentity3()))
		assertThat(newIdentities, empty())
		assertThat(removedIdentities, empty())
		assertThat(changedIdentities, containsInAnyOrder(identity2))
		assertThat(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity3()))
	}

	@Test
	fun `detect that a property was removed`() {
		val identity1 = createIdentity1()
		identity1.removeProperty("Key A")
		identityChangeDetector.detectChanges(listOf(identity1, createIdentity2(), createIdentity3()))
		assertThat(newIdentities, empty())
		assertThat(removedIdentities, empty())
		assertThat(changedIdentities, containsInAnyOrder(identity1))
		assertThat(unchangedIdentities, containsInAnyOrder(createIdentity2(), createIdentity3()))
	}

	@Test
	fun `detect that a property was added`() {
		val identity3 = createIdentity3()
		identity3.setProperty("Key A", "Value A")
		identityChangeDetector.detectChanges(listOf(createIdentity1(), createIdentity2(), identity3))
		assertThat(newIdentities, empty())
		assertThat(removedIdentities, empty())
		assertThat(changedIdentities, containsInAnyOrder(identity3))
		assertThat(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity2()))
	}

	@Test
	fun `detect that a property was changed`() {
		val identity3 = createIdentity3()
		identity3.setProperty("Key E", "Value F")
		identityChangeDetector.detectChanges(listOf(createIdentity1(), createIdentity2(), identity3))
		assertThat(newIdentities, empty())
		assertThat(removedIdentities, empty())
		assertThat(changedIdentities, containsInAnyOrder(identity3))
		assertThat(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity2()))
	}

	@Test
	fun `no removed identities are detected without an identity processor`() {
		identityChangeDetector.onRemovedIdentity = null
		identityChangeDetector.detectChanges(listOf(createIdentity1(), createIdentity3()))
		assertThat(removedIdentities, empty())
	}

	@Test
	fun `no added identities are detected without an identity processor`() {
		identityChangeDetector.onNewIdentity = null
		identityChangeDetector.detectChanges(listOf(createIdentity1(), createIdentity2(), createIdentity3(), createIdentity4()))
		assertThat(newIdentities, empty())
	}

	private fun createOldIdentities() =
			listOf(createIdentity1(), createIdentity2(), createIdentity3())

	private fun createIdentity1() =
			createIdentity("Test1", listOf("Context A", "Context B"), "Key A" to "Value A", "Key B" to "Value B")

	private fun createIdentity2() =
			createIdentity("Test2", listOf("Context C", "Context D"), "Key C" to "Value C", "Key D" to "Value D")

	private fun createIdentity3() =
			createIdentity("Test3", listOf("Context E", "Context F"), "Key E" to "Value E", "Key F" to "Value F")

	private fun createIdentity4() =
			createIdentity("Test4", listOf("Context G", "Context H"), "Key G" to "Value G", "Key H" to "Value H")

}
