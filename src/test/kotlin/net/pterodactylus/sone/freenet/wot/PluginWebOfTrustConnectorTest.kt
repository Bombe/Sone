package net.pterodactylus.sone.freenet.wot

import freenet.support.*
import freenet.support.api.*
import net.pterodactylus.sone.freenet.*
import net.pterodactylus.sone.freenet.plugin.*
import net.pterodactylus.sone.test.*
import org.hamcrest.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.hamcrest.core.*
import kotlin.test.*

/**
 * Unit test for [PluginWebOfTrustConnector].
 */
class PluginWebOfTrustConnectorTest {

	private val ownIdentity = DefaultOwnIdentity("id", "nickname", "requestUri", "insertUri")
	private val identity = DefaultIdentity("id-a", "alpha", "url://alpha")

	@Test
	fun `wot plugin can be pinged`() {
		createPluginConnector("Ping")
				.connect { ping() }
	}

	@Test
	fun `own identities are returned correctly`() {
		val ownIdentities = createPluginConnector("GetOwnIdentities") {
			put("Identity0", "id-0")
			put("RequestURI0", "request-uri-0")
			put("InsertURI0", "insert-uri-0")
			put("Nickname0", "nickname-0")
			put("Contexts0.Context0", "id-0-context-0")
			put("Properties0.Property0.Name", "id-0-property-0-name")
			put("Properties0.Property0.Value", "id-0-property-0-value")
			put("Identity1", "id-1")
			put("RequestURI1", "request-uri-1")
			put("InsertURI1", "insert-uri-1")
			put("Nickname1", "nickname-1")
			put("Contexts1.Context0", "id-1-context-0")
			put("Properties1.Property0.Name", "id-1-property-0-name")
			put("Properties1.Property0.Value", "id-1-property-0-value")
		}.connect { loadAllOwnIdentities() }
		assertThat(ownIdentities, containsInAnyOrder(
				isOwnIdentity("id-0", "nickname-0", "request-uri-0", "insert-uri-0", contains("id-0-context-0"), hasEntry("id-0-property-0-name", "id-0-property-0-value")),
				isOwnIdentity("id-1", "nickname-1", "request-uri-1", "insert-uri-1", contains("id-1-context-0"), hasEntry("id-1-property-0-name", "id-1-property-0-value"))
		))
	}

	@Test
	fun `trusted identities are requested with correct own identity`() {
		createPluginConnector("GetIdentitiesByScore", hasField("Truster", equalTo("id")))
				.connect { loadTrustedIdentities(ownIdentity) }
	}

	@Test
	fun `trusted identities are requested with correct selection parameter`() {
		createPluginConnector("GetIdentitiesByScore", hasField("Selection", equalTo("+")))
				.connect { loadTrustedIdentities(ownIdentity) }
	}

	@Test
	fun `trusted identities are requested with empty context if null context requested`() {
		createPluginConnector("GetIdentitiesByScore", hasField("Context", equalTo("")))
				.connect { loadTrustedIdentities(ownIdentity) }
	}

	@Test
	fun `trusted identities are requested with context if context requested`() {
		createPluginConnector("GetIdentitiesByScore", hasField("Context", equalTo("TestContext")))
				.connect { loadTrustedIdentities(ownIdentity, "TestContext") }
	}

	@Test
	fun `trusted identities are requested with trust values`() {
		createPluginConnector("GetIdentitiesByScore", hasField("WantTrustValues", equalTo("true")))
				.connect { loadTrustedIdentities(ownIdentity) }
	}

	@Test
	fun `empty list of trusted identities is returned correctly`() {
		val trustedIdentities = createPluginConnector("GetIdentitiesByScore")
				.connect { loadTrustedIdentities(ownIdentity) }
		assertThat(trustedIdentities, empty())
	}

	@Test
	fun `trusted identities without context, properties, or trust value are returned correctly`() {
		val trustedIdentities = createPluginConnector("GetIdentitiesByScore") {
			put("Identity0", "id0")
			put("Nickname0", "nickname0")
			put("RequestURI0", "request-uri0")
			put("Identity1", "id1")
			put("Nickname1", "nickname1")
			put("RequestURI1", "request-uri1")
		}.connect { loadTrustedIdentities(ownIdentity) }
		assertThat(trustedIdentities, contains(
				allOf(
						isIdentity("id0", "nickname0", "request-uri0", empty<String>(), isEmptyMap()),
						isTrusted(ownIdentity, isTrust(null, 0, 0))
				),
				allOf(
						isIdentity("id1", "nickname1", "request-uri1", empty<String>(), isEmptyMap()),
						isTrusted(ownIdentity, isTrust(null, 0, 0))
				)
		))
	}

	@Test
	fun `trusted identity with contexts is returned correctly`() {
		val trustedIdentities = createPluginConnector("GetIdentitiesByScore") {
			put("Identity0", "id0")
			put("Nickname0", "nickname0")
			put("RequestURI0", "request-uri0")
			put("Contexts0.Context0", "Context0")
			put("Contexts0.Context1", "Context1")
		}.connect { loadTrustedIdentities(ownIdentity) }
		assertThat(trustedIdentities, contains(
				isIdentity("id0", "nickname0", "request-uri0", containsInAnyOrder("Context0", "Context1"), isEmptyMap())
		))
	}

	@Test
	fun `trusted identity with properties is returned correctly`() {
		val trustedIdentities = createPluginConnector("GetIdentitiesByScore") {
			put("Identity0", "id0")
			put("Nickname0", "nickname0")
			put("RequestURI0", "request-uri0")
			put("Properties0.Property0.Name", "foo")
			put("Properties0.Property0.Value", "bar")
			put("Properties0.Property1.Name", "baz")
			put("Properties0.Property1.Value", "quo")
		}.connect { loadTrustedIdentities(ownIdentity) }
		assertThat(trustedIdentities, contains(
				isIdentity("id0", "nickname0", "request-uri0", empty(), allOf(hasEntry("foo", "bar"), hasEntry("baz", "quo")))
		))
	}

	@Test
	fun `trusted identity with trust value is returned correctly`() {
		val trustedIdentities = createPluginConnector("GetIdentitiesByScore") {
			put("Identity0", "id0")
			put("Nickname0", "nickname0")
			put("RequestURI0", "request-uri0")
			put("Trust0", "12")
			put("Score0", "34")
			put("Rank0", "56")
		}.connect { loadTrustedIdentities(ownIdentity) }
		assertThat(trustedIdentities, contains(
				allOf(
						isIdentity("id0", "nickname0", "request-uri0", empty(), isEmptyMap()),
						isTrusted(ownIdentity, isTrust(12, 34, 56))
				)
		))
	}

	@Test
	fun `adding a context sends the correct own identity id`() {
		createPluginConnector("AddContext", hasField("Identity", equalTo(ownIdentity.id)))
				.connect { addContext(ownIdentity, "TestContext") }
	}

	@Test
	fun `adding a context sends the correct context`() {
		createPluginConnector("AddContext", hasField("Context", equalTo("TestContext")))
				.connect { addContext(ownIdentity, "TestContext") }
	}

	@Test
	fun `removing a context sends the correct own identity id`() {
		createPluginConnector("RemoveContext", hasField("Identity", equalTo(ownIdentity.id)))
				.connect { removeContext(ownIdentity, "TestContext") }
	}

	@Test
	fun `removing a context sends the correct context`() {
		createPluginConnector("RemoveContext", hasField("Context", equalTo("TestContext")))
				.connect { removeContext(ownIdentity, "TestContext") }
	}

	@Test
	fun `setting a property sends the correct identity id`() {
		createPluginConnector("SetProperty", hasField("Identity", equalTo(ownIdentity.id)))
				.connect { setProperty(ownIdentity, "TestProperty", "TestValue") }
	}

	@Test
	fun `setting a property sends the correct property name`() {
		createPluginConnector("SetProperty", hasField("Property", equalTo("TestProperty")))
				.connect { setProperty(ownIdentity, "TestProperty", "TestValue") }
	}

	@Test
	fun `setting a property sends the correct property value`() {
		createPluginConnector("SetProperty", hasField("Value", equalTo("TestValue")))
				.connect { setProperty(ownIdentity, "TestProperty", "TestValue") }
	}

	@Test
	fun `removing a property sends the correct identity id`() {
		createPluginConnector("RemoveProperty", hasField("Identity", equalTo(ownIdentity.id)))
				.connect { removeProperty(ownIdentity, "TestProperty") }
	}

	@Test
	fun `removing a property sends the correct property name`() {
		createPluginConnector("RemoveProperty", hasField("Property", equalTo("TestProperty")))
				.connect { removeProperty(ownIdentity, "TestProperty") }
	}

	@Test
	fun `getting trust sends correct own identity id`() {
		createPluginConnector("GetIdentity", hasField("Truster", equalTo(ownIdentity.id)))
				.connect { getTrust(ownIdentity, identity) }
	}

	@Test
	fun `getting trust sends correct identity id`() {
		createPluginConnector("GetIdentity", hasField("Identity", equalTo(identity.id)))
				.connect { getTrust(ownIdentity, identity) }
	}

	@Test
	fun `getting trust returns correct trust values`() {
		val trust = createPluginConnector("GetIdentity", hasField("Identity", equalTo(identity.id))) {
			put("Trust", "12")
			put("Score", "34")
			put("Rank", "56")
		}.connect { getTrust(ownIdentity, identity) }
		assertThat(trust, isTrust(12, 34, 56))
	}

	@Test
	fun `getting trust reads incorrect numbers for trust as null`() {
		val trust = createPluginConnector("GetIdentity", hasField("Identity", equalTo(identity.id))) {
			put("Trust", "incorrect")
			put("Score", "34")
			put("Rank", "56")
		}.connect { getTrust(ownIdentity, identity) }
		assertThat(trust, isTrust(null, 34, 56))
	}

	@Test
	fun `getting trust reads incorrect numbers for score as null`() {
		val trust = createPluginConnector("GetIdentity", hasField("Identity", equalTo(identity.id))) {
			put("Trust", "12")
			put("Score", "incorrect")
			put("Rank", "56")
		}.connect { getTrust(ownIdentity, identity) }
		assertThat(trust, isTrust(12, null, 56))
	}

	@Test
	fun `getting trust reads incorrect numbers for rank as null`() {
		val trust = createPluginConnector("GetIdentity", hasField("Identity", equalTo(identity.id))) {
			put("Trust", "12")
			put("Score", "34")
			put("Rank", "incorrect")
		}.connect { getTrust(ownIdentity, identity) }
		assertThat(trust, isTrust(12, 34, null))
	}

	@Test
	fun `setting trust sends correct own identity id`() {
		createPluginConnector("SetTrust", hasField("Truster", equalTo(ownIdentity.id)))
				.connect { setTrust(ownIdentity, identity, 123, "Test Trust") }
	}

	@Test
	fun `setting trust sends correct identity id`() {
		createPluginConnector("SetTrust", hasField("Trustee", equalTo(identity.id)))
				.connect { setTrust(ownIdentity, identity, 123, "Test Trust") }
	}

	@Test
	fun `setting trust sends correct trust value`() {
		createPluginConnector("SetTrust", hasField("Value", equalTo("123")))
				.connect { setTrust(ownIdentity, identity, 123, "Test Trust") }
	}

	@Test
	fun `setting trust sends correct comment`() {
		createPluginConnector("SetTrust", hasField("Comment", equalTo("Test Trust")))
				.connect { setTrust(ownIdentity, identity, 123, "Test Trust") }
	}

	@Test
	fun `removing trust sends correct own identity id`() {
		createPluginConnector("RemoveTrust", hasField("Truster", equalTo(ownIdentity.id)))
				.connect { removeTrust(ownIdentity, identity) }
	}

	@Test
	fun `removing trust sends correct identity id`() {
		createPluginConnector("RemoveTrust", hasField("Trustee", equalTo(identity.id)))
				.connect { removeTrust(ownIdentity, identity) }
	}

}

private fun <R> PluginConnector.connect(block: PluginWebOfTrustConnector.() -> R) =
		PluginWebOfTrustConnector(this).let(block)

fun createPluginConnector(message: String, fieldsMatcher: Matcher<SimpleFieldSet> = IsAnything<SimpleFieldSet>(), build: SimpleFieldSetBuilder.() -> Unit = {}) =
		object : PluginConnector {
			override fun sendRequest(pluginName: String, identifier: String, fields: SimpleFieldSet, data: Bucket?) =
					if ((pluginName != wotPluginName) || (fields.get("Message") != message)) {
						throw PluginException()
					} else {
						assertThat(fields, fieldsMatcher)
						PluginReply(SimpleFieldSetBuilder().apply(build).get(), null)
					}
		}

private const val wotPluginName = "plugins.WebOfTrust.WebOfTrust"
