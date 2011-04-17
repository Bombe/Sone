/*
 * Sone - ConnectorListener.java - Copyright © 2010 David Roden
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

import java.util.EventListener;

import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 * Interface for objects that want to be notified if a {@link PluginConnector}
 * receives a reply from a plugin. As a connection listener is always
 * {@link PluginConnector#addConnectorListener(String, String, ConnectorListener)
 * added} for a specific plugin, it will always be notified for replies from the
 * correct plugin (unless you register the same listener for multiple
 * plugins—which you subsequently should not do).
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface ConnectorListener extends EventListener {

	/**
	 * A reply was received from the plugin this connection listener was added
	 * for.
	 *
	 * @param pluginConnector
	 *            The plugin connector that received the reply
	 * @param fields
	 *            The fields of the reply
	 * @param data
	 *            The data of the reply (may be null)
	 */
	public void receivedReply(PluginConnector pluginConnector, SimpleFieldSet fields, Bucket data);

}
