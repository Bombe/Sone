/*
 * Sone - IdentityListenerManager.java - Copyright © 2010 David Roden
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

import net.pterodactylus.util.event.AbstractListenerManager;

/**
 * Manager for {@link IdentityListener}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IdentityListenerManager extends AbstractListenerManager<IdentityManager, IdentityListener> {

	/**
	 * Creates a new identity listener manager.
	 */
	public IdentityListenerManager() {
		super(null);
	}

	//
	// ACTIONS
	//

	/**
	 * Notifies all listeners that an {@link OwnIdentity} that was not known on
	 * the previous check is available.
	 *
	 * @see IdentityListener#ownIdentityAdded(OwnIdentity)
	 * @param ownIdentity
	 *            The new own identity
	 */
	public void fireOwnIdentityAdded(OwnIdentity ownIdentity) {
		for (IdentityListener identityListener : getListeners()) {
			identityListener.ownIdentityAdded(ownIdentity);
		}
	}

	/**
	 * Notifies all listeners that an {@link OwnIdentity} that was available
	 * during the last check has gone away.
	 *
	 * @see IdentityListener#ownIdentityRemoved(OwnIdentity)
	 * @param ownIdentity
	 *            The disappeared own identity
	 */
	public void fireOwnIdentityRemoved(OwnIdentity ownIdentity) {
		for (IdentityListener identityListener : getListeners()) {
			identityListener.ownIdentityRemoved(ownIdentity);
		}
	}

	/**
	 * Notifies all listeners that a new identity was discovered.
	 *
	 * @see IdentityListener#identityAdded(OwnIdentity, Identity)
	 * @param ownIdentity
	 *            The own identity at the root of the trust tree
	 * @param identity
	 *            The new identity
	 */
	public void fireIdentityAdded(OwnIdentity ownIdentity, Identity identity) {
		for (IdentityListener identityListener : getListeners()) {
			identityListener.identityAdded(ownIdentity, identity);
		}
	}

	/**
	 * Notifies all listeners that some properties of the identity have changed.
	 *
	 * @see IdentityListener#identityUpdated(OwnIdentity, Identity)
	 * @param ownIdentity
	 *            The own identity at the root of the trust tree
	 * @param identity
	 *            The updated identity
	 */
	public void fireIdentityUpdated(OwnIdentity ownIdentity, Identity identity) {
		for (IdentityListener identityListener : getListeners()) {
			identityListener.identityUpdated(ownIdentity, identity);
		}
	}

	/**
	 * Notifies all listeners that an identity has gone away.
	 *
	 * @see IdentityListener#identityRemoved(OwnIdentity, Identity)
	 * @param ownIdentity
	 *            The own identity at the root of the trust tree
	 * @param identity
	 *            The disappeared identity
	 */
	public void fireIdentityRemoved(OwnIdentity ownIdentity, Identity identity) {
		for (IdentityListener identityListener : getListeners()) {
			identityListener.identityRemoved(ownIdentity, identity);
		}
	}

}
