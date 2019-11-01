/*
 * Sone - DefaultIdentityTest.java - Copyright © 2013–2019 David Roden
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

import com.google.common.collect.ImmutableMap.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.test.Matchers.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.hamcrest.collection.IsIterableContainingInOrder.contains
import org.junit.*

/**
 * Unit test for [DefaultIdentity].
 */
open class DefaultIdentityTest {

	protected open val identity = DefaultIdentity("Id", "Nickname", "RequestURI")

	@Test
	fun `identity can be created`() {
		assertThat(identity.id, equalTo("Id"))
		assertThat(identity.nickname, equalTo("Nickname"))
		assertThat(identity.requestUri, equalTo("RequestURI"))
		assertThat(identity.contexts, empty())
		assertThat(identity.properties, equalTo(emptyMap()))
	}

	@Test
	fun `contexts are added correctly`() {
		identity.addContext("Test")
		assertThat(identity.contexts, contains("Test"))
		assertThat(identity.hasContext("Test"), equalTo(true))
	}

	@Test
	fun `contexts are removed correctly`() {
		identity.addContext("Test")
		identity.removeContext("Test")
		assertThat(identity.contexts, empty())
		assertThat(identity.hasContext("Test"), equalTo(false))
	}

	@Test
	fun `contexts are set correctly in bulk`() {
		identity.addContext("Test")
		identity.contexts = setOf("Test1", "Test2")
		assertThat(identity.contexts, containsInAnyOrder("Test1", "Test2"))
		assertThat(identity.hasContext("Test"), equalTo(false))
		assertThat(identity.hasContext("Test1"), equalTo(true))
		assertThat(identity.hasContext("Test2"), equalTo(true))
	}

	@Test
	fun `properties are added correctly`() {
		identity.setProperty("Key", "Value")
		assertThat(identity.properties.size, equalTo(1))
		assertThat(identity.properties, hasEntry("Key", "Value"))
		assertThat(identity.getProperty("Key"), equalTo("Value"))
	}

	@Test
	fun `properties are removed correctly`() {
		identity.setProperty("Key", "Value")
		identity.removeProperty("Key")
		assertThat(identity.properties, equalTo(emptyMap()))
		assertThat(identity.getProperty("Key"), nullValue())
	}

	@Test
	fun `properties are set correctly in bulk`() {
		identity.setProperty("Key", "Value")
		identity.properties = of("Key1", "Value1", "Key2", "Value2")
		assertThat(identity.properties.size, equalTo(2))
		assertThat(identity.getProperty("Key"), nullValue())
		assertThat(identity.getProperty("Key1"), equalTo("Value1"))
		assertThat(identity.getProperty("Key2"), equalTo("Value2"))
	}

	@Test
	fun `trust relationships are added correctly`() {
		val ownIdentity = mock<OwnIdentity>()
		val trust = mock<Trust>()
		identity.setTrust(ownIdentity, trust)
		assertThat(identity.getTrust(ownIdentity), equalTo(trust))
	}

	@Test
	fun `trust relationships are removed correctly`() {
		val ownIdentity = mock<OwnIdentity>()
		val trust = mock<Trust>()
		identity.setTrust(ownIdentity, trust)
		identity.removeTrust(ownIdentity)
		assertThat(identity.getTrust(ownIdentity), nullValue())
	}

	@Test
	fun `identities with the same id are equal`() {
		val identity2 = DefaultIdentity("Id", "Nickname2", "RequestURI2")
		assertThat(identity2, equalTo(identity))
		assertThat(identity, equalTo(identity2))
	}

	@Test
	fun `two equal identities have the same hash code`() {
		val identity2 = DefaultIdentity("Id", "Nickname2", "RequestURI2")
		assertThat(identity.hashCode(), equalTo(identity2.hashCode()))
	}

	@Test
	fun `null does not match an identity`() {
		assertThat(identity, not(equalTo<Any>(null as Any?)))
	}

	@Test
	fun `toString() contains id and nickname`() {
		val identityString = identity.toString()
		assertThat(identityString, matchesRegex(".*\\bId\\b.*"))
		assertThat(identityString, matchesRegex(".*\\bNickname\\b.*"))
	}

}
