/*
 * Sone - SoneEvent.java - Copyright © 2013–2016 David Roden
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

package net.pterodactylus.sone.core.event;

import net.pterodactylus.sone.data.Sone;

/**
 * Base class for Sone events.
 */
public abstract class SoneEvent {

	/** The Sone this event is about. */
	private final Sone sone;

	/**
	 * Creates a new Sone event.
	 *
	 * @param sone
	 *            The Sone this event is about
	 */
	protected SoneEvent(Sone sone) {
		this.sone = sone;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the Sone this event is about.
	 *
	 * @return The Sone this event is about
	 */
	public Sone sone() {
		return sone;
	}

}
