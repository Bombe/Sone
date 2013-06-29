/*
 * Sone - IdentityRemovedEvent.java - Copyright © 2013 David Roden
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
 * Event that signals that an {@link Identity} was removed.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IdentityRemovedEvent extends IdentityEvent {

	/**
	 * Creates a new “identity removed” event.
	 *
	 * @param ownIdentity
	 *            The own identity that removed the identity
	 * @param identity
	 *            The identity that was removed
	 */
	public IdentityRemovedEvent(OwnIdentity ownIdentity, Identity identity) {
		super(ownIdentity, identity);
	}

}
