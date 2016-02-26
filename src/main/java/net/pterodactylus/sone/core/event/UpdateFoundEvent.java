/*
 * Sone - UpdateFoundEvent.java - Copyright © 2013–2015 David Roden
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

	private final Version version;
	private final long releaseTime;
	private final long latestEdition;
	private final boolean disruptive;

	public UpdateFoundEvent(Version version, long releaseTime, long latestEdition, boolean disruptive) {
		this.version = version;
		this.releaseTime = releaseTime;
		this.latestEdition = latestEdition;
		this.disruptive = disruptive;
	}

	public Version version() {
		return version;
	}

	public long releaseTime() {
		return releaseTime;
	}

	public long latestEdition() {
		return latestEdition;
	}

	public boolean disruptive() {
		return disruptive;
	}

}
