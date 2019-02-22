/*
 * Sone - IdentityEvent.java - Copyright © 2013–2019 David Roden
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

import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;

/**
 * Base class for {@link Identity} events.
 */
public abstract class IdentityEvent {

	/** The own identity this event relates to. */
	private final OwnIdentity ownIdentity;

	/** The identity this event is about. */
	private final Identity identity;

	/**
	 * Creates a new identity-based event.
	 *
	 * @param ownIdentity
	 *            The own identity that relates to the identity
	 * @param identity
	 *            The identity this event is about
	 */
	protected IdentityEvent(OwnIdentity ownIdentity, Identity identity) {
		this.ownIdentity = ownIdentity;
		this.identity = identity;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the own identity this event relates to.
	 *
	 * @return The own identity this event relates to
	 */
	public OwnIdentity ownIdentity() {
		return ownIdentity;
	}

	/**
	 * Returns the identity this event is about.
	 *
	 * @return The identity this event is about
	 */
	public Identity identity() {
		return identity;
	}

	@Override
	public int hashCode() {
		return ownIdentity().hashCode() ^ identity().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if ((object == null) || !object.getClass().equals(getClass())) {
			return false;
		}
		IdentityEvent identityEvent = (IdentityEvent) object;
		return ownIdentity().equals(identityEvent.ownIdentity()) && identity().equals(identityEvent.identity());
	}

}
