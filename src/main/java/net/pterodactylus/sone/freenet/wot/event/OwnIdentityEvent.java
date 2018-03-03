/*
 * Sone - OwnIdentityEvent.java - Copyright © 2013–2016 David Roden
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

package net.pterodactylus.sone.freenet.wot.event;

import net.pterodactylus.sone.freenet.wot.OwnIdentity;

/**
 * Base class for {@link OwnIdentity} events.
 */
public abstract class OwnIdentityEvent {

	/** The own identity this event is about. */
	private final OwnIdentity ownIdentity;

	/**
	 * Creates a new own identity-based event.
	 *
	 * @param ownIdentity
	 *            The own identity this event is about
	 */
	protected OwnIdentityEvent(OwnIdentity ownIdentity) {
		this.ownIdentity = ownIdentity;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the own identity this event is about.
	 *
	 * @return The own identity this event is about
	 */
	public OwnIdentity ownIdentity() {
		return ownIdentity;
	}

	@Override
	public int hashCode() {
		return ownIdentity().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if ((object == null) || !object.getClass().equals(getClass())) {
			return false;
		}
		OwnIdentityEvent ownIdentityEvent = (OwnIdentityEvent) object;
		return ownIdentity().equals(ownIdentityEvent.ownIdentity());
	}

}
