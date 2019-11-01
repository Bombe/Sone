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
 * Unit test for [WebOfTrustConnector].
 */
class WebOfTrustConnectorTest {

	private val ownIdentity = DefaultOwnIdentity("id", "nickname", "requestUri", "insertUri")

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
		val pluginConnector = createPluginConnector("GetIdentitiesByScore", hasField("Truster", equalTo("id")))
				.connect { loadTrustedIdentities(ownIdentity) }
	}

	@Test
	fun `trusted identities are requested with correct selection parameter`() {
		val pluginConnector = createPluginConnector("GetIdentitiesByScore", hasField("Selection", equalTo("+")))
				.connect { loadTrustedIdentities(ownIdentity) }
	}

	@Test
	fun `trusted identities are requested with empty context if null context requested`() {
		val pluginConnector = createPluginConnector("GetIdentitiesByScore", hasField("Context", equalTo("")))
				.connect { loadTrustedIdentities(ownIdentity) }
	}

	@Test
	fun `trusted identities are requested with context if context requested`() {
		val pluginConnector = createPluginConnector("GetIdentitiesByScore", hasField("Context", equalTo("TestContext")))
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

}

private fun <R> PluginConnector.connect(block: WebOfTrustConnector.() -> R) =
		WebOfTrustConnector(this).let(block)

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
