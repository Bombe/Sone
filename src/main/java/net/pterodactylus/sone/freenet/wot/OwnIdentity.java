/*
 * Sone - OwnIdentity.java - Copyright © 2010 David Roden
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

/**
 * An own identity is an identity that the owner of the node has full control
 * over.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class OwnIdentity extends Identity {

	/** The insert URI of the identity. */
	private final String insertUri;

	/**
	 * Creates a new own identity.
	 *
	 * @param id
	 *            The ID of the identity
	 * @param nickname
	 *            The nickname of the identity
	 * @param requestUri
	 *            The request URI of the identity
	 * @param insertUri
	 *            The insert URI of the identity
	 */
	public OwnIdentity(String id, String nickname, String requestUri, String insertUri) {
		super(id, nickname, requestUri);
		this.insertUri = insertUri;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the insert URI of the identity.
	 *
	 * @return The insert URI of the identity
	 */
	public String getInsertUri() {
		return insertUri;
	}

	/**
	 * Adds the given context to this identity.
	 *
	 * @param context
	 *            The context to add
	 */
	public void addContext(String context) {
		if (contexts.add(context)) {
			/* TODO - add. */
		}
	}

	/**
	 * Removes the given context from this identity.
	 *
	 * @param context
	 *            The context to remove
	 */
	public void removeContext(String context) {
		if (contexts.remove(context)) {
			/* TODO - remove */
		}
	}

	//
	// OBJECT METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[id=" + getId() + ",nickname=" + getNickname() + ",requestUri=" + getRequestUri() + ",insertUri=" + insertUri + "]";
	}

}
