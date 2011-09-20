/*
 * Sone - SoneInsertListenerManager.java - Copyright © 2011 David Roden
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

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.event.AbstractListenerManager;

/**
 * Manager for {@link SoneInsertListener}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneInsertListenerManager extends AbstractListenerManager<Sone, SoneInsertListener> {

	/**
	 * Creates a new Sone insert listener manager.
	 *
	 * @param sone
	 *            The sone being inserted
	 */
	public SoneInsertListenerManager(Sone sone) {
		super(sone);
	}

	//
	// ACTIONS
	//

	/**
	 * Notifies all listeners that the insert of the Sone has started.
	 *
	 * @see SoneInsertListener#insertStarted(Sone)
	 */
	void fireInsertStarted() {
		for (SoneInsertListener soneInsertListener : getListeners()) {
			soneInsertListener.insertStarted(getSource());
		}
	}

	/**
	 * Notifies all listeners that the insert of the Sone has finished
	 * successfully.
	 *
	 * @see SoneInsertListener#insertFinished(Sone, long)
	 * @param insertDuration
	 *            The insert duration (in milliseconds)
	 */
	void fireInsertFinished(long insertDuration) {
		for (SoneInsertListener soneInsertListener : getListeners()) {
			soneInsertListener.insertFinished(getSource(), insertDuration);
		}
	}

	/**
	 * Notifies all listeners that the insert of the Sone was aborted.
	 *
	 * @see SoneInsertListener#insertAborted(Sone, Throwable)
	 * @param cause
	 *            The cause of the abortion (may be {@code null}
	 */
	void fireInsertAborted(Throwable cause) {
		for (SoneInsertListener soneInsertListener : getListeners()) {
			soneInsertListener.insertAborted(getSource(), cause);
		}
	}

}
