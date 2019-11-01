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
import freenet.support.api.*
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
@Singleton
class WebOfTrustConnector @Inject constructor(private val pluginConnector: PluginConnector) {

	private val logger: Logger = getLogger(WebOfTrustConnector::class.java.name)

	fun stop() {
		/* does nothing. */
	}

	/**
	 * Loads all own identities from the Web of Trust plugin.
	 *
	 * @return All own identity
	 * @throws WebOfTrustException if the own identities can not be loaded
	 */
	@Throws(WebOfTrustException::class)
	fun loadAllOwnIdentities(): Set<OwnIdentity> {
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

	/**
	 * Loads all identities that the given identities trusts with a score of
	 * more than 0 and the (optional) given context.
	 *
	 * @param ownIdentity The own identity
	 * @param context The context to filter, or `null`
	 * @return All trusted identities
	 * @throws PluginException if an error occured talking to the Web of Trust plugin
	 */
	@Throws(PluginException::class)
	@JvmOverloads
	fun loadTrustedIdentities(ownIdentity: OwnIdentity, context: String? = null): Set<Identity> {
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

	/**
	 * Adds the given context to the given identity.
	 *
	 * @param ownIdentity The identity to add the context to
	 * @param context The context to add
	 * @throws PluginException if an error occured talking to the Web of Trust plugin
	 */
	@Throws(PluginException::class)
	fun addContext(ownIdentity: OwnIdentity, context: String) {
		performRequest(SimpleFieldSetBuilder().put("Message", "AddContext").put("Identity", ownIdentity.id).put("Context", context).get())
	}

	/**
	 * Removes the given context from the given identity.
	 *
	 * @param ownIdentity The identity to remove the context from
	 * @param context The context to remove
	 * @throws PluginException if an error occured talking to the Web of Trust plugin
	 */
	@Throws(PluginException::class)
	fun removeContext(ownIdentity: OwnIdentity, context: String) {
		performRequest(SimpleFieldSetBuilder().put("Message", "RemoveContext").put("Identity", ownIdentity.id).put("Context", context).get())
	}

	/**
	 * Sets the property with the given name to the given value.
	 *
	 * @param ownIdentity The identity to set the property on
	 * @param name The name of the property to set
	 * @param value The value to set
	 * @throws PluginException if an error occured talking to the Web of Trust plugin
	 */
	@Throws(PluginException::class)
	fun setProperty(ownIdentity: OwnIdentity, name: String, value: String) {
		performRequest(SimpleFieldSetBuilder().put("Message", "SetProperty").put("Identity", ownIdentity.id).put("Property", name).put("Value", value).get())
	}

	/**
	 * Removes the property with the given name.
	 *
	 * @param ownIdentity The identity to remove the property from
	 * @param name The name of the property to remove
	 * @throws PluginException if an error occured talking to the Web of Trust plugin
	 */
	@Throws(PluginException::class)
	fun removeProperty(ownIdentity: OwnIdentity, name: String) {
		performRequest(SimpleFieldSetBuilder().put("Message", "RemoveProperty").put("Identity", ownIdentity.id).put("Property", name).get())
	}

	/**
	 * Returns the trust for the given identity assigned to it by the given own
	 * identity.
	 *
	 * @param ownIdentity The own identity
	 * @param identity The identity to get the trust for
	 * @return The trust for the given identity
	 * @throws PluginException if an error occured talking to the Web of Trust plugin
	 */
	@Throws(PluginException::class)
	fun getTrust(ownIdentity: OwnIdentity, identity: Identity): Trust {
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

	/**
	 * Sets the trust for the given identity.
	 *
	 * @param ownIdentity The trusting identity
	 * @param identity The trusted identity
	 * @param trust The amount of trust (-100 thru 100)
	 * @param comment The comment or explanation of the trust value
	 * @throws PluginException if an error occured talking to the Web of Trust plugin
	 */
	@Throws(PluginException::class)
	fun setTrust(ownIdentity: OwnIdentity, identity: Identity, trust: Int, comment: String) {
		performRequest(SimpleFieldSetBuilder().put("Message", "SetTrust").put("Truster", ownIdentity.id).put("Trustee", identity.id).put("Value", trust.toString()).put("Comment", comment).get())
	}

	/**
	 * Removes any trust assignment of the given own identity for the given
	 * identity.
	 *
	 * @param ownIdentity The own identity
	 * @param identity The identity to remove all trust for
	 * @throws WebOfTrustException if an error occurs
	 */
	@Throws(WebOfTrustException::class)
	fun removeTrust(ownIdentity: OwnIdentity, identity: Identity) {
		performRequest(SimpleFieldSetBuilder().put("Message", "RemoveTrust").put("Truster", ownIdentity.id).put("Trustee", identity.id).get())
	}

	/**
	 * Pings the Web of Trust plugin. If the plugin can not be reached, a
	 * [PluginException] is thrown.
	 *
	 * @throws PluginException if the plugin is not loaded
	 */
	@Throws(PluginException::class)
	fun ping() {
		performRequest(SimpleFieldSetBuilder().put("Message", "Ping").get())
	}

	private fun performRequest(fields: SimpleFieldSet, data: Bucket? = null): PluginReply {
		logger.log(Level.FINE, format("Sending FCP Request: %s", fields.get("Message")))
		val pluginReply = pluginConnector.sendRequest(WOT_PLUGIN_NAME, "", fields, data)
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

