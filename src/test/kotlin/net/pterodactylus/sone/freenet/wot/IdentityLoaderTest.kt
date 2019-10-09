/*
 * Sone - IdentityLoaderTest.java - Copyright © 2013–2019 David Roden
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

import com.google.common.base.Optional.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [IdentityLoader].
 */
class IdentityLoaderTest {

	private val webOfTrustConnector = mock<WebOfTrustConnector>()
	private val identityLoader = IdentityLoader(webOfTrustConnector, Context("Test"))
	private val identityLoaderWithoutContext = IdentityLoader(webOfTrustConnector)

	@Before
	fun setup() {
		val ownIdentities = createOwnIdentities()
		whenever(webOfTrustConnector.loadAllOwnIdentities()).thenReturn(ownIdentities.toSet())
		whenever(webOfTrustConnector.loadTrustedIdentities(eq(ownIdentities[0]), any())).thenReturn(createTrustedIdentitiesForFirstOwnIdentity())
		whenever(webOfTrustConnector.loadTrustedIdentities(eq(ownIdentities[1]), any())).thenReturn(createTrustedIdentitiesForSecondOwnIdentity())
		whenever(webOfTrustConnector.loadTrustedIdentities(eq(ownIdentities[2]), any())).thenReturn(createTrustedIdentitiesForThirdOwnIdentity())
		whenever(webOfTrustConnector.loadTrustedIdentities(eq(ownIdentities[3]), any())).thenReturn(createTrustedIdentitiesForFourthOwnIdentity())
	}

	private fun createOwnIdentities() = listOf(
			createOwnIdentity("O1", "ON1", "OR1", "OI1", listOf("Test", "Test2"), mapOf("KeyA" to "ValueA", "KeyB" to "ValueB")),
			createOwnIdentity("O2", "ON2", "OR2", "OI2", listOf("Test"), mapOf("KeyC" to "ValueC")),
			createOwnIdentity("O3", "ON3", "OR3", "OI3", listOf("Test2"), mapOf("KeyE" to "ValueE", "KeyD" to "ValueD")),
			createOwnIdentity("O4", "ON4", "OR$", "OI4", listOf("Test"), mapOf("KeyA" to "ValueA", "KeyD" to "ValueD"))
	)

	private fun createTrustedIdentitiesForFirstOwnIdentity() = setOf(
			createIdentity("I11", "IN11", "IR11", listOf("Test"), mapOf("KeyA" to "ValueA"))
	)

	private fun createTrustedIdentitiesForSecondOwnIdentity() = setOf(
			createIdentity("I21", "IN21", "IR21", listOf("Test", "Test2"), mapOf("KeyB" to "ValueB"))
	)

	private fun createTrustedIdentitiesForThirdOwnIdentity() = setOf(
			createIdentity("I31", "IN31", "IR31", listOf("Test", "Test3"), mapOf("KeyC" to "ValueC"))
	)

	private fun createTrustedIdentitiesForFourthOwnIdentity(): Set<Identity> = emptySet()

	private fun createOwnIdentity(id: String, nickname: String, requestUri: String, insertUri: String, contexts: List<String>, properties: Map<String, String>): OwnIdentity =
			DefaultOwnIdentity(id, nickname, requestUri, insertUri).apply {
				setContexts(contexts)
				this.setProperties(properties)
			}

	private fun createIdentity(id: String, nickname: String, requestUri: String, contexts: List<String>, properties: Map<String, String>): Identity =
			DefaultIdentity(id, nickname, requestUri).apply {
				setContexts(contexts)
				this.properties = properties
			}

	@Test
	fun loadingIdentities() {
		val ownIdentities = createOwnIdentities()
		val identities = identityLoader.loadIdentities()
		verify(webOfTrustConnector).loadAllOwnIdentities()
		verify(webOfTrustConnector).loadTrustedIdentities(eq(ownIdentities[0]), eq("Test"))
		verify(webOfTrustConnector).loadTrustedIdentities(eq(ownIdentities[1]), eq("Test"))
		verify(webOfTrustConnector, never()).loadTrustedIdentities(eq(ownIdentities[2]), any())
		verify(webOfTrustConnector).loadTrustedIdentities(eq(ownIdentities[3]), eq("Test"))
		assertThat(identities.keys, hasSize(4))
		assertThat(identities.keys, containsInAnyOrder(ownIdentities[0], ownIdentities[1], ownIdentities[2], ownIdentities[3]))
		verifyIdentitiesForOwnIdentity(identities, ownIdentities[0], createTrustedIdentitiesForFirstOwnIdentity())
		verifyIdentitiesForOwnIdentity(identities, ownIdentities[1], createTrustedIdentitiesForSecondOwnIdentity())
		verifyIdentitiesForOwnIdentity(identities, ownIdentities[2], emptySet())
		verifyIdentitiesForOwnIdentity(identities, ownIdentities[3], createTrustedIdentitiesForFourthOwnIdentity())
	}

	@Test
	fun loadingIdentitiesWithoutContext() {
		val ownIdentities = createOwnIdentities()
		val identities = identityLoaderWithoutContext.loadIdentities()
		verify(webOfTrustConnector).loadAllOwnIdentities()
		verify(webOfTrustConnector).loadTrustedIdentities(eq(ownIdentities[0]), eq(null))
		verify(webOfTrustConnector).loadTrustedIdentities(eq(ownIdentities[1]), eq(null))
		verify(webOfTrustConnector).loadTrustedIdentities(eq(ownIdentities[2]), eq(null))
		verify(webOfTrustConnector).loadTrustedIdentities(eq(ownIdentities[3]), eq(null))
		assertThat(identities.keys, hasSize(4))
		val firstOwnIdentity = ownIdentities[0]
		val secondOwnIdentity = ownIdentities[1]
		val thirdOwnIdentity = ownIdentities[2]
		val fourthOwnIdentity = ownIdentities[3]
		assertThat(identities.keys, containsInAnyOrder(firstOwnIdentity, secondOwnIdentity, thirdOwnIdentity, fourthOwnIdentity))
		verifyIdentitiesForOwnIdentity(identities, firstOwnIdentity, createTrustedIdentitiesForFirstOwnIdentity())
		verifyIdentitiesForOwnIdentity(identities, secondOwnIdentity, createTrustedIdentitiesForSecondOwnIdentity())
		verifyIdentitiesForOwnIdentity(identities, thirdOwnIdentity, createTrustedIdentitiesForThirdOwnIdentity())
		verifyIdentitiesForOwnIdentity(identities, fourthOwnIdentity, createTrustedIdentitiesForFourthOwnIdentity())
	}

	private fun verifyIdentitiesForOwnIdentity(identities: Map<OwnIdentity, Collection<Identity>>, ownIdentity: OwnIdentity, trustedIdentities: Set<Identity>) {
		assertThat(identities[ownIdentity], equalTo<Collection<Identity>>(trustedIdentities))
	}

}
