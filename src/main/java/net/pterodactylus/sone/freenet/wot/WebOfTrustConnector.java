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

package net.pterodactylus.sone.freenet.wot;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.*;
import javax.annotation.*;

import net.pterodactylus.sone.freenet.plugin.*;

import com.google.inject.*;
import freenet.support.*;
import freenet.support.api.*;

import static java.lang.String.*;
import static java.util.logging.Logger.*;
import static net.pterodactylus.sone.utils.NumberParsers.*;

/**
 * Connector for the Web of Trust plugin.
 */
@Singleton
public class WebOfTrustConnector {

	/** The logger. */
	private static final Logger logger = getLogger(WebOfTrustConnector.class.getName());

	/** The name of the WoT plugin. */
	private static final String WOT_PLUGIN_NAME = "plugins.WebOfTrust.WebOfTrust";

	/** The plugin connector. */
	private final PluginConnector pluginConnector;

	/**
	 * Creates a new Web of Trust connector that uses the given plugin
	 * connector.
	 *
	 * @param pluginConnector
	 *            The plugin connector
	 */
	@Inject
	public WebOfTrustConnector(PluginConnector pluginConnector) {
		this.pluginConnector = pluginConnector;
	}

	//
	// ACTIONS
	//

	/**
	 * Stops the web of trust connector.
	 */
	public void stop() {
		/* does nothing. */
	}

	/**
	 * Loads all own identities from the Web of Trust plugin.
	 *
	 * @return All own identity
	 * @throws WebOfTrustException
	 *             if the own identities can not be loaded
	 */
	public Set<OwnIdentity> loadAllOwnIdentities() throws WebOfTrustException {
		PluginReply reply = performRequest(SimpleFieldSetConstructor.create().put("Message", "GetOwnIdentities").get());
		SimpleFieldSet fields = reply.getFields();
		int ownIdentityCounter = -1;
		Set<OwnIdentity> ownIdentities = new HashSet<>();
		while (true) {
			String id = fields.get("Identity" + ++ownIdentityCounter);
			if (id == null) {
				break;
			}
			String requestUri = fields.get("RequestURI" + ownIdentityCounter);
			String insertUri = fields.get("InsertURI" + ownIdentityCounter);
			String nickname = fields.get("Nickname" + ownIdentityCounter);
			DefaultOwnIdentity ownIdentity = new DefaultOwnIdentity(id, nickname, requestUri, insertUri);
			ownIdentity.setContexts(parseContexts("Contexts" + ownIdentityCounter + ".", fields));
			ownIdentity.setProperties(parseProperties("Properties" + ownIdentityCounter + ".", fields));
			ownIdentities.add(ownIdentity);
		}
		return ownIdentities;
	}

	/**
	 * Loads all identities that the given identities trusts with a score of
	 * more than 0.
	 *
	 * @param ownIdentity
	 *            The own identity
	 * @return All trusted identities
	 * @throws PluginException
	 *             if an error occured talking to the Web of Trust plugin
	 */
	public Set<Identity> loadTrustedIdentities(OwnIdentity ownIdentity) throws PluginException {
		return loadTrustedIdentities(ownIdentity, null);
	}

	/**
	 * Loads all identities that the given identities trusts with a score of
	 * more than 0 and the (optional) given context.
	 *
	 * @param ownIdentity
	 *            The own identity
	 * @param context
	 *            The context to filter, or {@code null}
	 * @return All trusted identities
	 * @throws PluginException
	 *             if an error occured talking to the Web of Trust plugin
	 */
	public Set<Identity> loadTrustedIdentities(OwnIdentity ownIdentity, @Nullable String context) throws PluginException {
		PluginReply reply = performRequest(SimpleFieldSetConstructor.create().put("Message", "GetIdentitiesByScore").put("Truster", ownIdentity.getId()).put("Selection", "+").put("Context", (context == null) ? "" : context).put("WantTrustValues", "true").get());
		SimpleFieldSet fields = reply.getFields();
		Set<Identity> identities = new HashSet<>();
		int identityCounter = -1;
		while (true) {
			String id = fields.get("Identity" + ++identityCounter);
			if (id == null) {
				break;
			}
			String nickname = fields.get("Nickname" + identityCounter);
			String requestUri = fields.get("RequestURI" + identityCounter);
			DefaultIdentity identity = new DefaultIdentity(id, nickname, requestUri);
			identity.setContexts(parseContexts("Contexts" + identityCounter + ".", fields));
			identity.setProperties(parseProperties("Properties" + identityCounter + ".", fields));
			Integer trust = parseInt(fields.get("Trust" + identityCounter), null);
			int score = parseInt(fields.get("Score" + identityCounter), 0);
			int rank = parseInt(fields.get("Rank" + identityCounter), 0);
			identity.setTrust(ownIdentity, new Trust(trust, score, rank));
			identities.add(identity);
		}
		return identities;
	}

	/**
	 * Adds the given context to the given identity.
	 *
	 * @param ownIdentity
	 *            The identity to add the context to
	 * @param context
	 *            The context to add
	 * @throws PluginException
	 *             if an error occured talking to the Web of Trust plugin
	 */
	public void addContext(OwnIdentity ownIdentity, String context) throws PluginException {
		performRequest(SimpleFieldSetConstructor.create().put("Message", "AddContext").put("Identity", ownIdentity.getId()).put("Context", context).get());
	}

	/**
	 * Removes the given context from the given identity.
	 *
	 * @param ownIdentity
	 *            The identity to remove the context from
	 * @param context
	 *            The context to remove
	 * @throws PluginException
	 *             if an error occured talking to the Web of Trust plugin
	 */
	public void removeContext(OwnIdentity ownIdentity, String context) throws PluginException {
		performRequest(SimpleFieldSetConstructor.create().put("Message", "RemoveContext").put("Identity", ownIdentity.getId()).put("Context", context).get());
	}

	/**
	 * Sets the property with the given name to the given value.
	 *
	 * @param ownIdentity
	 *            The identity to set the property on
	 * @param name
	 *            The name of the property to set
	 * @param value
	 *            The value to set
	 * @throws PluginException
	 *             if an error occured talking to the Web of Trust plugin
	 */
	public void setProperty(OwnIdentity ownIdentity, String name, String value) throws PluginException {
		performRequest(SimpleFieldSetConstructor.create().put("Message", "SetProperty").put("Identity", ownIdentity.getId()).put("Property", name).put("Value", value).get());
	}

	/**
	 * Removes the property with the given name.
	 *
	 * @param ownIdentity
	 *            The identity to remove the property from
	 * @param name
	 *            The name of the property to remove
	 * @throws PluginException
	 *             if an error occured talking to the Web of Trust plugin
	 */
	public void removeProperty(OwnIdentity ownIdentity, String name) throws PluginException {
		performRequest(SimpleFieldSetConstructor.create().put("Message", "RemoveProperty").put("Identity", ownIdentity.getId()).put("Property", name).get());
	}

	/**
	 * Returns the trust for the given identity assigned to it by the given own
	 * identity.
	 *
	 * @param ownIdentity
	 *            The own identity
	 * @param identity
	 *            The identity to get the trust for
	 * @return The trust for the given identity
	 * @throws PluginException
	 *             if an error occured talking to the Web of Trust plugin
	 */
	public Trust getTrust(OwnIdentity ownIdentity, Identity identity) throws PluginException {
		PluginReply getTrustReply = performRequest(SimpleFieldSetConstructor.create().put("Message", "GetIdentity").put("Truster", ownIdentity.getId()).put("Identity", identity.getId()).get());
		String trust = getTrustReply.getFields().get("Trust");
		String score = getTrustReply.getFields().get("Score");
		String rank = getTrustReply.getFields().get("Rank");
		Integer explicit = null;
		Integer implicit = null;
		Integer distance = null;
		try {
			explicit = Integer.valueOf(trust);
		} catch (NumberFormatException nfe1) {
			/* ignore. */
		}
		try {
			implicit = Integer.valueOf(score);
			distance = Integer.valueOf(rank);
		} catch (NumberFormatException nfe1) {
			/* ignore. */
		}
		return new Trust(explicit, implicit, distance);
	}

	/**
	 * Sets the trust for the given identity.
	 *
	 * @param ownIdentity
	 *            The trusting identity
	 * @param identity
	 *            The trusted identity
	 * @param trust
	 *            The amount of trust (-100 thru 100)
	 * @param comment
	 *            The comment or explanation of the trust value
	 * @throws PluginException
	 *             if an error occured talking to the Web of Trust plugin
	 */
	public void setTrust(OwnIdentity ownIdentity, Identity identity, int trust, String comment) throws PluginException {
		performRequest(SimpleFieldSetConstructor.create().put("Message", "SetTrust").put("Truster", ownIdentity.getId()).put("Trustee", identity.getId()).put("Value", String.valueOf(trust)).put("Comment", comment).get());
	}

	/**
	 * Removes any trust assignment of the given own identity for the given
	 * identity.
	 *
	 * @param ownIdentity
	 *            The own identity
	 * @param identity
	 *            The identity to remove all trust for
	 * @throws WebOfTrustException
	 *             if an error occurs
	 */
	public void removeTrust(OwnIdentity ownIdentity, Identity identity) throws WebOfTrustException {
		performRequest(SimpleFieldSetConstructor.create().put("Message", "RemoveTrust").put("Truster", ownIdentity.getId()).put("Trustee", identity.getId()).get());
	}

	/**
	 * Pings the Web of Trust plugin. If the plugin can not be reached, a
	 * {@link PluginException} is thrown.
	 *
	 * @throws PluginException
	 *             if the plugin is not loaded
	 */
	public void ping() throws PluginException {
		performRequest(SimpleFieldSetConstructor.create().put("Message", "Ping").get());
	}

	//
	// PRIVATE ACTIONS
	//

	/**
	 * Parses the contexts from the given fields.
	 *
	 * @param prefix
	 *            The prefix to use to access the contexts
	 * @param fields
	 *            The fields to parse the contexts from
	 * @return The parsed contexts
	 */
	private static Set<String> parseContexts(String prefix, SimpleFieldSet fields) {
		Set<String> contexts = new HashSet<>();
		int contextCounter = -1;
		while (true) {
			String context = fields.get(prefix + "Context" + ++contextCounter);
			if (context == null) {
				break;
			}
			contexts.add(context);
		}
		return contexts;
	}

	/**
	 * Parses the properties from the given fields.
	 *
	 * @param prefix
	 *            The prefix to use to access the properties
	 * @param fields
	 *            The fields to parse the properties from
	 * @return The parsed properties
	 */
	private static Map<String, String> parseProperties(String prefix, SimpleFieldSet fields) {
		Map<String, String> properties = new HashMap<>();
		int propertiesCounter = -1;
		while (true) {
			String propertyName = fields.get(prefix + "Property" + ++propertiesCounter + ".Name");
			if (propertyName == null) {
				break;
			}
			String propertyValue = fields.get(prefix + "Property" + propertiesCounter + ".Value");
			properties.put(propertyName, propertyValue);
		}
		return properties;
	}

	/**
	 * Sends a request containing the given fields and waits for the target
	 * message.
	 *
	 * @param fields
	 *            The fields of the message
	 * @return The reply message
	 * @throws PluginException
	 *             if the request could not be sent
	 */
	private PluginReply performRequest(SimpleFieldSet fields) throws PluginException {
		return performRequest(fields, null);
	}

	/**
	 * Sends a request containing the given fields and waits for the target
	 * message.
	 *
	 * @param fields
	 *            The fields of the message
	 * @param data
	 *            The payload of the message
	 * @return The reply message
	 * @throws PluginException
	 *             if the request could not be sent
	 */
	private PluginReply performRequest(SimpleFieldSet fields, Bucket data) throws PluginException {
		logger.log(Level.FINE, format("Sending FCP Request: %s", fields.get("Message")));
		PluginReply pluginReply = pluginConnector.sendRequest(WOT_PLUGIN_NAME, "", fields, data);
		logger.log(Level.FINEST, format("Received FCP Response for %s: %s", fields.get("Message"), pluginReply.getFields().get("Message")));
		if ("Error".equals(pluginReply.getFields().get("Message"))) {
			throw new PluginException("Could not perform request for " + fields.get("Message"));
		}
		return pluginReply;
	}

	/**
	 * Helper method to create {@link SimpleFieldSet}s with terser code.
	 */
	private static class SimpleFieldSetConstructor {

		/** The field set being created. */
		private SimpleFieldSet simpleFieldSet;

		/**
		 * Creates a new simple field set constructor.
		 *
		 * @param shortLived
		 *            {@code true} if the resulting simple field set should be
		 *            short-lived, {@code false} otherwise
		 */
		private SimpleFieldSetConstructor(boolean shortLived) {
			simpleFieldSet = new SimpleFieldSet(shortLived);
		}

		//
		// ACCESSORS
		//

		/**
		 * Returns the created simple field set.
		 *
		 * @return The created simple field set
		 */
		public SimpleFieldSet get() {
			return simpleFieldSet;
		}

		/**
		 * Sets the field with the given name to the given value.
		 *
		 * @param name
		 *            The name of the fleld
		 * @param value
		 *            The value of the field
		 * @return This constructor (for method chaining)
		 */
		public SimpleFieldSetConstructor put(String name, String value) {
			simpleFieldSet.putOverwrite(name, value);
			return this;
		}

		//
		// ACTIONS
		//

		/**
		 * Creates a new simple field set constructor.
		 *
		 * @return The created simple field set constructor
		 */
		public static SimpleFieldSetConstructor create() {
			return create(true);
		}

		/**
		 * Creates a new simple field set constructor.
		 *
		 * @param shortLived
		 *            {@code true} if the resulting simple field set should be
		 *            short-lived, {@code false} otherwise
		 * @return The created simple field set constructor
		 */
		public static SimpleFieldSetConstructor create(boolean shortLived) {
			return new SimpleFieldSetConstructor(shortLived);
		}

	}

}
