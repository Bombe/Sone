/*
 * Sone - PluginConnector.java - Copyright © 2010 David Roden
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
import java.util.Map;

import net.pterodactylus.util.collection.Pair;
import freenet.pluginmanager.FredPluginTalker;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginRespirator;
import freenet.pluginmanager.PluginTalker;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 * Interface for talking to other plugins. Other plugins are identified by their
 * name and a unique connection identifier.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PluginConnector implements FredPluginTalker {

	/** The plugin respirator. */
	private final PluginRespirator pluginRespirator;

	/** Connector listener managers for all plugin connections. */
	private final Map<Pair<String, String>, ConnectorListenerManager> connectorListenerManagers = Collections.synchronizedMap(new HashMap<Pair<String, String>, ConnectorListenerManager>());

	/**
	 * Creates a new plugin connector.
	 *
	 * @param pluginRespirator
	 *            The plugin respirator
	 */
	public PluginConnector(PluginRespirator pluginRespirator) {
		this.pluginRespirator = pluginRespirator;
	}

	//
	// LISTENER MANAGEMENT
	//

	/**
	 * Adds a connection listener for the given plugin connection.
	 *
	 * @param pluginName
	 *            The name of the plugin
	 * @param identifier
	 *            The identifier of the connection
	 * @param connectorListener
	 *            The listener to add
	 */
	public void addConnectorListener(String pluginName, String identifier, ConnectorListener connectorListener) {
		getConnectorListenerManager(pluginName, identifier).addListener(connectorListener);
	}

	/**
	 * Removes a connection listener for the given plugin connection.
	 *
	 * @param pluginName
	 *            The name of the plugin
	 * @param identifier
	 *            The identifier of the connection
	 * @param connectorListener
	 *            The listener to remove
	 */
	public void removeConnectorListener(String pluginName, String identifier, ConnectorListener connectorListener) {
		getConnectorListenerManager(pluginName, identifier).removeListener(connectorListener);
	}

	//
	// ACTIONS
	//

	/**
	 * Sends a request to the given plugin.
	 *
	 * @param pluginName
	 *            The name of the plugin
	 * @param identifier
	 *            The identifier of the connection
	 * @param fields
	 *            The fields of the message
	 * @throws PluginException
	 *             if the plugin can not be found
	 */
	public void sendRequest(String pluginName, String identifier, SimpleFieldSet fields) throws PluginException {
		sendRequest(pluginName, identifier, fields, null);
	}

	/**
	 * Sends a request to the given plugin.
	 *
	 * @param pluginName
	 *            The name of the plugin
	 * @param identifier
	 *            The identifier of the connection
	 * @param fields
	 *            The fields of the message
	 * @param data
	 *            The payload of the message (may be null)
	 * @throws PluginException
	 *             if the plugin can not be found
	 */
	public void sendRequest(String pluginName, String identifier, SimpleFieldSet fields, Bucket data) throws PluginException {
		getPluginTalker(pluginName, identifier).send(fields, data);
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Returns the connection listener manager for the given plugin connection,
	 * creating a new one if none does exist yet.
	 *
	 * @param pluginName
	 *            The name of the plugin
	 * @param identifier
	 *            The identifier of the connection
	 * @return The connection listener manager
	 */
	private ConnectorListenerManager getConnectorListenerManager(String pluginName, String identifier) {
		return getConnectorListenerManager(pluginName, identifier, true);
	}

	/**
	 * Returns the connection listener manager for the given plugin connection,
	 * optionally creating a new one if none does exist yet.
	 *
	 * @param pluginName
	 *            The name of the plugin
	 * @param identifier
	 *            The identifier of the connection
	 * @param create
	 *            {@code true} to create a new manager if there is none,
	 *            {@code false} to return {@code null} in that case
	 * @return The connection listener manager, or {@code null} if none existed
	 *         and {@code create} is {@code false}
	 */
	private ConnectorListenerManager getConnectorListenerManager(String pluginName, String identifier, boolean create) {
		ConnectorListenerManager connectorListenerManager = connectorListenerManagers.get(new Pair<String, String>(pluginName, identifier));
		if (create && (connectorListenerManager == null)) {
			connectorListenerManager = new ConnectorListenerManager(this);
			connectorListenerManagers.put(new Pair<String, String>(pluginName, identifier), connectorListenerManager);
		}
		return connectorListenerManager;
	}

	/**
	 * Returns the plugin talker for the given plugin connection.
	 *
	 * @param pluginName
	 *            The name of the plugin
	 * @param identifier
	 *            The identifier of the connection
	 * @return The plugin talker
	 * @throws PluginException
	 *             if the plugin can not be found
	 */
	private PluginTalker getPluginTalker(String pluginName, String identifier) throws PluginException {
		try {
			return pluginRespirator.getPluginTalker(this, pluginName, identifier);
		} catch (PluginNotFoundException pnfe1) {
			throw new PluginException(pnfe1);
		}
	}

	//
	// INTERFACE FredPluginTalker
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onReply(String pluginName, String identifier, SimpleFieldSet params, Bucket data) {
		ConnectorListenerManager connectorListenerManager = getConnectorListenerManager(pluginName, identifier, false);
		if (connectorListenerManager == null) {
			/* we don’t care about events for this plugin. */
			return;
		}
		connectorListenerManager.fireReceivedReply(params, data);
	}

}
