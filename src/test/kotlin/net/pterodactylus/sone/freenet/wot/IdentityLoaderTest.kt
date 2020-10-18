/*
 * Sone - IdentityLoaderTest.kt - Copyright © 2013–2020 David Roden
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

import net.pterodactylus.sone.test.createIdentity
import net.pterodactylus.sone.test.createOwnIdentity
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [IdentityLoader].
 */
class IdentityLoaderTest {

	@Test
	fun `loading all identities merges remote identities’ trust values`() {
		val ownIdentity1 = createOwnIdentity("o1")
		val ownIdentity2 = createOwnIdentity("o2")
		val webOfTrustConnector = dummyWebOfTrustConnector
				.overrideLoadAllOwnIdentities { setOf(ownIdentity1, ownIdentity2) }
				.overrideLoadAllIdentities { ownIdentity, _ ->
					when (ownIdentity) {
						ownIdentity1 -> setOf(createIdentity().setTrust(ownIdentity1, Trust(100, 50, 2)))
						else -> setOf(createIdentity().setTrust(ownIdentity2, Trust(80, 40, 2)))
					}
				}
		val identityLoader = IdentityLoader(webOfTrustConnector)
		val allIdentities = identityLoader.loadAllIdentities()
		assertThat(allIdentities[ownIdentity1]!!.first().trust[ownIdentity2], equalTo(Trust(80, 40, 2)))
		assertThat(allIdentities[ownIdentity2]!!.first().trust[ownIdentity1], equalTo(Trust(100, 50, 2)))
	}

}
