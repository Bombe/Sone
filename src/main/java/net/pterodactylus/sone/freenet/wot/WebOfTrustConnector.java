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
		Reply reply = performRequest("OwnIdentities", SimpleFieldSetConstructor.create().put("Message", "GetOwnIdentities").get());
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
			ownIdentities.add(ownIdentity);
		}
		return ownIdentities;
	}

	//
	// PRIVATE ACTIONS
	//

	/**
	 * Sends a request containing the given fields and waits for the target
	 * message.
	 *
	 * @param targetMessage
	 *            The message of the reply to wait for
	 * @param fields
	 *            The fields of the message
	 * @return The reply message
	 * @throws PluginException
	 *             if the request could not be sent
	 */
	private Reply performRequest(String targetMessage, SimpleFieldSet fields) throws PluginException {
		return performRequest(targetMessage, fields, null);
	}

	/**
	 * Sends a request containing the given fields and waits for the target
	 * message.
	 *
	 * @param targetMessage
	 *            The message of the reply to wait for
	 * @param fields
	 *            The fields of the message
	 * @param data
	 *            The payload of the message
	 * @return The reply message
	 * @throws PluginException
	 *             if the request could not be sent
	 */
	private Reply performRequest(String targetMessage, SimpleFieldSet fields, Bucket data) throws PluginException {
		@SuppressWarnings("synthetic-access")
		Reply reply = new Reply();
		replies.put(targetMessage, reply);
		synchronized (reply) {
			pluginConnector.sendRequest(WOT_PLUGIN_NAME, PLUGIN_CONNECTION_IDENTIFIER, fields, data);
			try {
				reply.wait();
			} catch (InterruptedException ie1) {
				logger.log(Level.WARNING, "Got interrupted while waiting for reply on GetOwnIdentities.", ie1);
			}
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
