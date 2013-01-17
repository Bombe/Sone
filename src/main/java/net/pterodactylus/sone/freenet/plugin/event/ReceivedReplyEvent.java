/*
 * Sone - ReceivedReplyEvent.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sone.freenet.plugin.event;

import net.pterodactylus.sone.freenet.plugin.PluginConnector;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 * Event that signals that a plugin reply was received.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ReceivedReplyEvent {

	/** The connector that received the reply. */
	private final PluginConnector pluginConnector;

	/** The name of the plugin that sent the reply. */
	private final String pluginName;

	/** The identifier of the initial request. */
	private final String identifier;

	/** The fields containing the reply. */
	private final SimpleFieldSet fieldSet;

	/** The optional reply data. */
	private final Bucket data;

	/**
	 * Creates a new “reply received” event.
	 *
	 * @param pluginConnector
	 *            The connector that received the event
	 * @param pluginName
	 *            The name of the plugin that sent the reply
	 * @param identifier
	 *            The identifier of the initial request
	 * @param fieldSet
	 *            The fields containing the reply
	 * @param data
	 *            The optional data of the reply
	 */
	public ReceivedReplyEvent(PluginConnector pluginConnector, String pluginName, String identifier, SimpleFieldSet fieldSet, Bucket data) {
		this.pluginConnector = pluginConnector;
		this.pluginName = pluginName;
		this.identifier = identifier;
		this.fieldSet = fieldSet;
		this.data = data;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the plugin connector that received the reply.
	 *
	 * @return The plugin connector that received the reply
	 */
	public PluginConnector pluginConnector() {
		return pluginConnector;
	}

	/**
	 * Returns the name of the plugin that sent the reply.
	 *
	 * @return The name of the plugin that sent the reply
	 */
	public String pluginName() {
		return pluginName;
	}

	/**
	 * Returns the identifier of the initial request.
	 *
	 * @return The identifier of the initial request
	 */
	public String identifier() {
		return identifier;
	}

	/**
	 * Returns the fields containing the reply.
	 *
	 * @return The fields containing the reply
	 */
	public SimpleFieldSet fieldSet() {
		return fieldSet;
	}

	/**
	 * Returns the optional data of the reply.
	 *
	 * @return The optional data of the reply (may be {@code null})
	 */
	public Bucket data() {
		return data;
	}

}
