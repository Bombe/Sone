/*
 * Sone - UpdateListenerManager.java - Copyright © 2011 David Roden
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

package net.pterodactylus.sone.core;

import net.pterodactylus.util.event.AbstractListenerManager;
import net.pterodactylus.util.version.Version;

/**
 * Listener manager for {@link UpdateListener} events.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class UpdateListenerManager extends AbstractListenerManager<Void, UpdateListener> {

	/**
	 * Creates a new update listener manager.
	 */
	public UpdateListenerManager() {
		super(null);
	}

	//
	// ACTIONS
	//

	/**
	 * Notifies all listeners that a new version has been found.
	 *
	 * @param version
	 *            The new version
	 * @param releaseTime
	 *            The release time of the new version
	 */
	void fireUpdateFound(Version version, long releaseTime) {
		for (UpdateListener updateListener : getListeners()) {
			updateListener.updateFound(version, releaseTime);
		}
	}

}
