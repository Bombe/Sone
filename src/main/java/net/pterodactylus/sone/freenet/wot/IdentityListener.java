/*
 * Sone - IdentityListener.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.freenet.wot;

import java.util.EventListener;

/**
 * Listener interface for {@link IdentityManager} events.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface IdentityListener extends EventListener {

	/**
	 * Notifies a listener that an {@link OwnIdentity} that was not known on the
	 * previous check is available.
	 *
	 * @param ownIdentity
	 *            The new own identity
	 */
	public void ownIdentityAdded(OwnIdentity ownIdentity);

	/**
	 * Notifies a listener that an {@link OwnIdentity} that was available during
	 * the last check has gone away.
	 *
	 * @param ownIdentity
	 *            The disappeared own identity
	 */
	public void ownIdentityRemoved(OwnIdentity ownIdentity);

	/**
	 * Notifies a listener that a new identity was discovered.
	 *
	 * @param identity
	 *            The new identity
	 */
	public void identityAdded(Identity identity);

	/**
	 * Notifies a listener that some properties of the identity have changed.
	 *
	 * @param identity
	 *            The updated identity
	 */
	public void identityUpdated(Identity identity);

	/**
	 * Notifies a listener that an identity has gone away.
	 *
	 * @param identity
	 *            The disappeared identity
	 */
	public void identityRemoved(Identity identity);

}
