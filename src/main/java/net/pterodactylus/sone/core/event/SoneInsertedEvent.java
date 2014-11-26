/*
 * Sone - SoneInsertedEvent.java - Copyright © 2013 David Roden
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
 * Event that signals that a {@link Sone} was inserted.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneInsertedEvent extends SoneEvent {

	private final long insertDuration;

	public SoneInsertedEvent(Sone sone, long insertDuration) {
		super(sone);
		this.insertDuration = insertDuration;
	}

	public long insertDuration() {
		return insertDuration;
	}

}
