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
import net.pterodactylus.sone.freenet.*
import net.pterodactylus.sone.freenet.plugin.*
import net.pterodactylus.sone.utils.NumberParsers.*
import java.lang.String.*
import java.util.*
import java.util.logging.*
import java.util.logging.Logger
import java.util.logging.Logger.*

/**
 * Connector for the Web of Trust plugin.
 */
class PluginWebOfTrustConnector @Inject constructor(private val pluginConnector: PluginConnector) : WebOfTrustConnector {

	private val logger: Logger = getLogger(PluginWebOfTrustConnector::class.java.name)

	override fun loadAllOwnIdentities(): Set<OwnIdentity> {
		val (fields) = performRequest(SimpleFieldSetBuilder().put("Message", "GetOwnIdentities").get())
		var ownIdentityCounter = -1
		val ownIdentities = HashSet<OwnIdentity>()
		while (true) {
			val id = fields.get("Identity" + ++ownIdentityCounter) ?: break
			val requestUri = fields.get("RequestURI$ownIdentityCounter")
			val insertUri = fields.get("InsertURI$ownIdentityCounter")
			val nickname = fields.get("Nickname$ownIdentityCounter")
			val ownIdentity = DefaultOwnIdentity(id, nickname, requestUri, insertUri)
			ownIdentity.setContexts(parseContexts("Contexts$ownIdentityCounter.", fields))
			ownIdentity.properties = parseProperties("Properties$ownIdentityCounter.", fields)
			ownIdentities.add(ownIdentity)
		}
		return ownIdentities
	}

	override fun loadTrustedIdentities(ownIdentity: OwnIdentity, context: String?): Set<Identity> {
		val (fields) = performRequest(SimpleFieldSetBuilder().put("Message", "GetIdentitiesByScore").put("Truster", ownIdentity.id).put("Selection", "+").put("Context", context ?: "").put("WantTrustValues", "true").get())
		val identities = HashSet<Identity>()
		var identityCounter = -1
		while (true) {
			val id = fields.get("Identity" + ++identityCounter) ?: break
			val nickname = fields.get("Nickname$identityCounter")
			val requestUri = fields.get("RequestURI$identityCounter")
			val identity = DefaultIdentity(id, nickname, requestUri)
			identity.setContexts(parseContexts("Contexts$identityCounter.", fields))
			identity.properties = parseProperties("Properties$identityCounter.", fields)
			val trust = parseInt(fields.get("Trust$identityCounter"), null)
			val score = parseInt(fields.get("Score$identityCounter"), 0)!!
			val rank = parseInt(fields.get("Rank$identityCounter"), 0)!!
			identity.setTrust(ownIdentity, Trust(trust, score, rank))
			identities.add(identity)
		}
		return identities
	}

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

	override fun getTrust(ownIdentity: OwnIdentity, identity: Identity): Trust {
		val (fields) = performRequest(SimpleFieldSetBuilder().put("Message", "GetIdentity").put("Truster", ownIdentity.id).put("Identity", identity.id).get())
		val trust = fields.get("Trust")
		val score = fields.get("Score")
		val rank = fields.get("Rank")
		var explicit: Int? = null
		var implicit: Int? = null
		var distance: Int? = null
		try {
			explicit = Integer.valueOf(trust)
		} catch (nfe1: NumberFormatException) {
			/* ignore. */
		}

		try {
			implicit = Integer.valueOf(score)
		} catch (nfe1: NumberFormatException) {
			/* ignore. */
		}

		try {
			distance = Integer.valueOf(rank)
		} catch (nfe1: NumberFormatException) {
			/* ignore. */
		}

		return Trust(explicit, implicit, distance)
	}

	override fun setTrust(ownIdentity: OwnIdentity, identity: Identity, trust: Int, comment: String) {
		performRequest(SimpleFieldSetBuilder().put("Message", "SetTrust").put("Truster", ownIdentity.id).put("Trustee", identity.id).put("Value", trust.toString()).put("Comment", comment).get())
	}

	override fun removeTrust(ownIdentity: OwnIdentity, identity: Identity) {
		performRequest(SimpleFieldSetBuilder().put("Message", "RemoveTrust").put("Truster", ownIdentity.id).put("Trustee", identity.id).get())
	}

	override fun ping() {
		performRequest(SimpleFieldSetBuilder().put("Message", "Ping").get())
	}

	private fun performRequest(fields: SimpleFieldSet): PluginReply {
		logger.log(Level.FINE, format("Sending FCP Request: %s", fields.get("Message")))
		val pluginReply = pluginConnector.sendRequest(WOT_PLUGIN_NAME, "", fields)
		logger.log(Level.FINEST, format("Received FCP Response for %s: %s", fields.get("Message"), pluginReply.fields.get("Message")))
		if ("Error" == pluginReply.fields.get("Message")) {
			throw PluginException("Could not perform request for " + fields.get("Message"))
		}
		return pluginReply
	}

}

private const val WOT_PLUGIN_NAME = "plugins.WebOfTrust.WebOfTrust"

private fun parseContexts(prefix: String, fields: SimpleFieldSet): Set<String> {
	val contexts = HashSet<String>()
	var contextCounter = -1
	while (true) {
		val context = fields.get(prefix + "Context" + ++contextCounter) ?: break
		contexts.add(context)
	}
	return contexts
}

private fun parseProperties(prefix: String, fields: SimpleFieldSet): Map<String, String> {
	val properties = HashMap<String, String>()
	var propertiesCounter = -1
	while (true) {
		val propertyName = fields.get(prefix + "Property" + ++propertiesCounter + ".Name") ?: break
		val propertyValue = fields.get(prefix + "Property" + propertiesCounter + ".Value")
		properties[propertyName] = propertyValue
	}
	return properties
}
