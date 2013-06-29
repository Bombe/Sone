/*
 * Sone - UpdateFoundEvent.java - Copyright © 2013 David Roden
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

import net.pterodactylus.util.version.Version;

/**
 * Event that signals that an update for Sone was found.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class UpdateFoundEvent {

	/** The version that was found. */
	private final Version version;

	/** The time the update was released. */
	private final long releaseTime;

	/** The latest edition of the update page. */
	private final long latestEdition;

	/**
	 * Creates a new “update found” event.
	 *
	 * @param version
	 *            The version of the update
	 * @param releaseTime
	 *            The release time of the update
	 * @param latestEdition
	 *            The latest edition of the update page
	 */
	public UpdateFoundEvent(Version version, long releaseTime, long latestEdition) {
		this.version = version;
		this.releaseTime = releaseTime;
		this.latestEdition = latestEdition;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the version of the update.
	 *
	 * @return The version of the update
	 */
	public Version version() {
		return version;
	}

	/**
	 * Returns the release time of the update.
	 *
	 * @return The releae time of the update (in milliseconds since Jan 1, 1970
	 *         UTC)
	 */
	public long releaseTime() {
		return releaseTime;
	}

	/**
	 * Returns the latest edition of the update page.
	 *
	 * @return The latest edition of the update page
	 */
	public long latestEdition() {
		return latestEdition;
	}

}
