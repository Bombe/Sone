/*
 * Sone - WebOfTrustConnector.java - Copyright © 2010–2019 David Roden
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

import com.google.inject.*
import freenet.support.*
import kotlinx.coroutines.*
import net.pterodactylus.sone.freenet.*
import net.pterodactylus.sone.freenet.plugin.*
import java.lang.String.*
import java.util.logging.*
import java.util.logging.Logger
import java.util.logging.Logger.*

/**
 * Connector for the Web of Trust plugin.
 */
class PluginWebOfTrustConnector @Inject constructor(private val pluginConnector: PluginConnector) : WebOfTrustConnector {

	private val logger: Logger = getLogger(PluginWebOfTrustConnector::class.java.name)

	@Throws(PluginException::class)
	override fun loadAllOwnIdentities(): Set<OwnIdentity> =
			performRequest(SimpleFieldSetBuilder().put("Message", "GetOwnIdentities").get())
					.fields
					.parseIdentities { parseOwnIdentity(it) }

	@Throws(PluginException::class)
	override fun loadTrustedIdentities(ownIdentity: OwnIdentity, context: String?): Set<Identity> =
			performRequest(SimpleFieldSetBuilder().put("Message", "GetIdentitiesByScore").put("Truster", ownIdentity.id).put("Selection", "+").put("Context", context ?: "").put("WantTrustValues", "true").get())
					.fields
					.parseIdentities { parseTrustedIdentity(it, ownIdentity) }

	@Throws(PluginException::class)
	override fun addContext(ownIdentity: OwnIdentity, context: String) {
		performRequest(SimpleFieldSetBuilder().put("Message", "AddContext").put("Identity", ownIdentity.id).put("Context", context).get())
	}

	@Throws(PluginException::class)
	override fun removeContext(ownIdentity: OwnIdentity, context: String) {
		performRequest(SimpleFieldSetBuilder().put("Message", "RemoveContext").put("Identity", ownIdentity.id).put("Context", context).get())
	}

	override fun setProperty(ownIdentity: OwnIdentity, name: String, value: String) {
		performRequest(SimpleFieldSetBuilder().put("Message", "SetProperty").put("Identity", ownIdentity.id).put("Property", name).put("Value", value).get())
	}

	override fun removeProperty(ownIdentity: OwnIdentity, name: String) {
		performRequest(SimpleFieldSetBuilder().put("Message", "RemoveProperty").put("Identity", ownIdentity.id).put("Property", name).get())
	}

	override fun ping() {
		performRequest(SimpleFieldSetBuilder().put("Message", "Ping").get())
	}

	private fun performRequest(fields: SimpleFieldSet): PluginReply {
		logger.log(Level.FINE, format("Sending FCP Request: %s", fields.get("Message")))
		return runBlocking {
			pluginConnector.sendRequest(WOT_PLUGIN_NAME, fields).also {
				logger.log(Level.FINEST, format("Received FCP Response for %s: %s", fields.get("Message"), it.fields.get("Message")))
				if ("Error" == it.fields.get("Message")) {
					throw PluginException("Could not perform request for " + fields.get("Message"))
				}
			}
		}
	}

}

private const val WOT_PLUGIN_NAME = "plugins.WebOfTrust.WebOfTrust"

private fun <I> SimpleFieldSet.parseIdentities(parser: SimpleFieldSet.(Int) -> I) =
		scanPrefix { "Identity$it" }
				.map { parser(this, it) }
				.toSet()

private fun SimpleFieldSet.parseOwnIdentity(index: Int) =
		DefaultOwnIdentity(get("Identity$index"), get("Nickname$index"), get("RequestURI$index"), get("InsertURI$index"))
				.setContextsAndProperties(this@parseOwnIdentity, index)

private fun SimpleFieldSet.parseTrustedIdentity(index: Int, ownIdentity: OwnIdentity) =
		DefaultIdentity(get("Identity$index"), get("Nickname$index"), get("RequestURI$index"))
				.setContextsAndProperties(this@parseTrustedIdentity, index)
				.apply { setTrust(ownIdentity, this@parseTrustedIdentity.parseTrust(index.toString())) }

private fun <I : Identity> I.setContextsAndProperties(simpleFieldSet: SimpleFieldSet, index: Int) = apply {
	contexts = simpleFieldSet.contexts("Contexts$index.")
	properties = simpleFieldSet.properties("Properties$index.")
}

private fun SimpleFieldSet.parseTrust(index: String = "") =
		Trust(get("Trust$index")?.toIntOrNull(), get("Score$index")?.toIntOrNull(), get("Rank$index")?.toIntOrNull())

private fun SimpleFieldSet.contexts(prefix: String) =
		scanPrefix { "${prefix}Context$it" }
				.map { get("${prefix}Context$it") }
				.toSet()

private fun SimpleFieldSet.properties(prefix: String) =
		scanPrefix { "${prefix}Property${it}.Name" }
				.map { get("${prefix}Property${it}.Name") to get("${prefix}Property${it}.Value") }
				.toMap()

private fun SimpleFieldSet.scanPrefix(prefix: (Int) -> String) =
		generateSequence(0, Int::inc)
				.takeWhile { get(prefix(it)) != null }
