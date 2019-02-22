/*
 * Sone - SoneInsertAbortedEvent.java - Copyright © 2013–2019 David Roden
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
 * Event that signals that a {@link Sone} insert was aborted.
 */
public class SoneInsertAbortedEvent extends SoneEvent {

	/** The cause of the abortion. */
	private final Throwable cause;

	/**
	 * Creates a new “Sone was inserted” event.
	 *
	 * @param sone
	 *            The Sone that was inserted
	 * @param cause
	 *            The cause of the abortion
	 */
	public SoneInsertAbortedEvent(Sone sone, Throwable cause) {
		super(sone);
		this.cause = cause;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the cause of the abortion.
	 *
	 * @return The cause of the abortion (may be {@code null})
	 */
	public Throwable cause() {
		return cause;
	}

}
