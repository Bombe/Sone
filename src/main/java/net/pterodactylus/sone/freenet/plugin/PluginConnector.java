/*
 * Sone - PluginConnector.java - Copyright © 2010–2013 David Roden
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

package net.pterodactylus.sone.freenet.plugin;

import net.pterodactylus.sone.freenet.plugin.event.ReceivedReplyEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

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

	/** The event bus. */
	private final EventBus eventBus;

	/** The plugin respirator. */
	private final PluginRespirator pluginRespirator;

	/**
	 * Creates a new plugin connector.
	 *
	 * @param eventBus
	 *            The event bus
	 * @param pluginRespirator
	 *            The plugin respirator
	 */
	@Inject
	public PluginConnector(EventBus eventBus, PluginRespirator pluginRespirator) {
		this.eventBus = eventBus;
		this.pluginRespirator = pluginRespirator;
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
		eventBus.post(new ReceivedReplyEvent(this, pluginName, identifier, params, data));
	}

}
