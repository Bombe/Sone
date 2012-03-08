/*
 * Sone - ConnectorListenerManager.java - Copyright © 2010–2012 David Roden
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

import net.pterodactylus.util.event.AbstractListenerManager;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 * Manages {@link ConnectorListener}s and fire events.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ConnectorListenerManager extends AbstractListenerManager<PluginConnector, ConnectorListener> {

	/**
	 * Creates a new manager for {@link ConnectorListener}s.
	 *
	 * @param pluginConnector
	 *            The plugin connector that is the source for all events
	 */
	public ConnectorListenerManager(PluginConnector pluginConnector) {
		super(pluginConnector);
	}

	//
	// ACTIONS
	//

	/**
	 * Notifies all registered listeners that a reply from the plugin was
	 * received.
	 *
	 * @param fields
	 *            The fields of the reply
	 * @param data
	 *            The data of the reply (may be null)
	 */
	public void fireReceivedReply(SimpleFieldSet fields, Bucket data) {
		for (ConnectorListener connectorListener : getListeners()) {
			connectorListener.receivedReply(getSource(), fields, data);
		}
	}

}
