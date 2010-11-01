/*
 * Sone - WebOfTrustConnector.java - Copyright © 2010 David Roden
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.util.logging.Logging;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 * Connector for the Web of Trust plugin.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class WebOfTrustConnector implements ConnectorListener {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(WebOfTrustConnector.class);

	/** The name of the WoT plugin. */
	private static final String WOT_PLUGIN_NAME = "plugins.WoT.WoT";

	/** A random connection identifier. */
	private static final String PLUGIN_CONNECTION_IDENTIFIER = "Sone-WoT-Connector-" + Math.abs(Math.random());

	/** The current replies that we wait for. */
	private final Map<String, Reply> replies = Collections.synchronizedMap(new HashMap<String, Reply>());

	/** The plugin connector. */
	private final PluginConnector pluginConnector;

	/**
	 * Creates a new Web of Trust connector that uses the given plugin
	 * connector.
	 *
	 * @param pluginConnector
	 *            The plugin connector
	 */
	public WebOfTrustConnector(PluginConnector pluginConnector) {
		this.pluginConnector = pluginConnector;
		pluginConnector.addConnectorListener(WOT_PLUGIN_NAME, PLUGIN_CONNECTION_IDENTIFIER, this);
	}

	//
	// ACTIONS
	//

	/**
	 * Loads all own identities from the Web of Trust plugin.
	 *
	 * @return All own identity
	 * @throws PluginException
	 *             if the own identities can not be loaded
	 */
	public Set<OwnIdentity> loadAllOwnIdentities() throws PluginException {
		Reply reply = performRequest(SimpleFieldSetConstructor.create().put("Message", "GetOwnIdentities").get(), "OwnIdentities");
		SimpleFieldSet fields = reply.getFields();
		int ownIdentityCounter = -1;
		Set<OwnIdentity> ownIdentities = new HashSet<OwnIdentity>();
		while (true) {
			String id = fields.get("Identity" + ++ownIdentityCounter);
			if (id == null) {
				break;
			}
			String requestUri = fields.get("RequestURI" + ownIdentityCounter);
			String insertUri = fields.get("InsertURI" + ownIdentityCounter);
			String nickname = fields.get("Nickname" + ownIdentityCounter);
			OwnIdentity ownIdentity = new OwnIdentity(id, nickname, requestUri, insertUri);
			ownIdentity.setContexts(parseContexts("Contexts" + ownIdentityCounter, fields));
			ownIdentity.setProperties(parseProperties("Properties" + ownIdentityCounter, fields));
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
	public Set<Identity> loadTrustedIdentities(OwnIdentity ownIdentity, String context) throws PluginException {
		Reply reply = performRequest(SimpleFieldSetConstructor.create().put("Message", "GetIdentitiesByScore").put("TreeOwner", ownIdentity.getId()).put("Selection", "+").put("Context", (context == null) ? "" : context).get(), "Identities");
		SimpleFieldSet fields = reply.getFields();
		Set<Identity> identities = new HashSet<Identity>();
		int identityCounter = -1;
		while (true) {
			String id = fields.get("Identity" + ++identityCounter);
			if (id == null) {
				break;
			}
			String nickname = fields.get("Nickname" + identityCounter);
			String requestUri = fields.get("RequestURI" + identityCounter);
			Identity identity = new Identity(id, nickname, requestUri);
			identity.setContexts(parseContexts("Contexts" + identityCounter, fields));
			identity.setProperties(parseProperties("Properties" + identityCounter, fields));
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
		performRequest(SimpleFieldSetConstructor.create().put("Message", "AddContext").put("Identity", ownIdentity.getId()).put("Context", context).get(), "ContextAdded");
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
		performRequest(SimpleFieldSetConstructor.create().put("Message", "RemoveContext").put("Identity", ownIdentity.getId()).put("Context", context).get(), "ContextRemoved");
	}

	/**
	 * Returns the value of the property with the given name.
	 *
	 * @param identity
	 *            The identity whose properties to check
	 * @param name
	 *            The name of the property to return
	 * @return The value of the property, or {@code null} if there is no value
	 * @throws PluginException
	 *             if an error occured talking to the Web of Trust plugin
	 */
	public String getProperty(Identity identity, String name) throws PluginException {
		Reply reply = performRequest(SimpleFieldSetConstructor.create().put("Message", "GetProperty").put("Identity", identity.getId()).put("Property", name).get(), "PropertyValue");
		return reply.getFields().get("Property");
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
		performRequest(SimpleFieldSetConstructor.create().put("Message", "SetProperty").put("Identity", ownIdentity.getId()).put("Property", name).put("Value", value).get(), "PropertyAdded");
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
		performRequest(SimpleFieldSetConstructor.create().put("Message", "RemoveProperty").put("Identity", ownIdentity.getId()).put("Property", name).get(), "PropertyRemoved");
	}

	/**
	 * Pings the Web of Trust plugin. If the plugin can not be reached, a
	 * {@link PluginException} is thrown.
	 *
	 * @throws PluginException
	 *             if the plugin is not loaded
	 */
	public void ping() throws PluginException {
		performRequest(SimpleFieldSetConstructor.create().put("Message", "Ping").get(), "Pong");
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
	private Set<String> parseContexts(String prefix, SimpleFieldSet fields) {
		Set<String> contexts = new HashSet<String>();
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
	private Map<String, String> parseProperties(String prefix, SimpleFieldSet fields) {
		Map<String, String> properties = new HashMap<String, String>();
		int propertiesCounter = -1;
		while (true) {
			String propertyName = fields.get(prefix + "Property" + ++propertiesCounter + "Name");
			if (propertyName == null) {
				break;
			}
			String propertyValue = fields.get(prefix + "Property" + propertiesCounter + "Value");
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
	 * @param targetMessages
	 *            The messages of the reply to wait for
	 * @return The reply message
	 * @throws PluginException
	 *             if the request could not be sent
	 */
	private Reply performRequest(SimpleFieldSet fields, String... targetMessages) throws PluginException {
		return performRequest(fields, null, targetMessages);
	}

	/**
	 * Sends a request containing the given fields and waits for the target
	 * message.
	 *
	 * @param fields
	 *            The fields of the message
	 * @param data
	 *            The payload of the message
	 * @param targetMessages
	 *            The messages of the reply to wait for
	 * @return The reply message
	 * @throws PluginException
	 *             if the request could not be sent
	 */
	private Reply performRequest(SimpleFieldSet fields, Bucket data, String... targetMessages) throws PluginException {
		@SuppressWarnings("synthetic-access")
		Reply reply = new Reply();
		for (String targetMessage : targetMessages) {
			replies.put(targetMessage, reply);
		}
		replies.put("Error", reply);
		synchronized (reply) {
			pluginConnector.sendRequest(WOT_PLUGIN_NAME, PLUGIN_CONNECTION_IDENTIFIER, fields, data);
			try {
				reply.wait(60000);
			} catch (InterruptedException ie1) {
				logger.log(Level.WARNING, "Got interrupted while waiting for reply on GetOwnIdentities.", ie1);
			}
		}
		for (String targetMessage : targetMessages) {
			replies.remove(targetMessage);
		}
		replies.remove("Error");
		if ((reply.getFields() != null) && reply.getFields().get("Message").equals("Error")) {
			throw new PluginException("Could not perform request for " + targetMessages[0]);
		}
		return reply;
	}

	//
	// INTERFACE ConnectorListener
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedReply(PluginConnector pluginConnector, SimpleFieldSet fields, Bucket data) {
		String messageName = fields.get("Message");
		Reply reply = replies.remove(messageName);
		if (reply == null) {
			logger.log(Level.FINE, "Not waiting for a “%s” message.", messageName);
			return;
		}
		synchronized (reply) {
			reply.setFields(fields);
			reply.setData(data);
			reply.notify();
		}
	}

	/**
	 * Container for the data of the reply from a plugin.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private static class Reply {

		/** The fields of the reply. */
		private SimpleFieldSet fields;

		/** The payload of the reply. */
		private Bucket data;

		/**
		 * Returns the fields of the reply.
		 *
		 * @return The fields of the reply
		 */
		public SimpleFieldSet getFields() {
			return fields;
		}

		/**
		 * Sets the fields of the reply.
		 *
		 * @param fields
		 *            The fields of the reply
		 */
		public void setFields(SimpleFieldSet fields) {
			this.fields = fields;
		}

		/**
		 * Returns the payload of the reply.
		 *
		 * @return The payload of the reply (may be {@code null})
		 */
		@SuppressWarnings("unused")
		public Bucket getData() {
			return data;
		}

		/**
		 * Sets the payload of the reply.
		 *
		 * @param data
		 *            The payload of the reply (may be {@code null})
		 */
		public void setData(Bucket data) {
			this.data = data;
		}

	}

	/**
	 * Helper method to create {@link SimpleFieldSet}s with terser code.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
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
			SimpleFieldSetConstructor simpleFieldSetConstructor = new SimpleFieldSetConstructor(shortLived);
			return simpleFieldSetConstructor;
		}

	}

}
