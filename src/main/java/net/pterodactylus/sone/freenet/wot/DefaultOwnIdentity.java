/*
 * Sone - DefaultOwnIdentity.java - Copyright © 2010–2019 David Roden
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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An own identity is an identity that the owner of the node has full control
 * over.
 */
public class DefaultOwnIdentity extends DefaultIdentity implements OwnIdentity {

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
	public DefaultOwnIdentity(String id, String nickname, String requestUri, String insertUri) {
		super(id, nickname, requestUri);
		this.insertUri = checkNotNull(insertUri);
	}

	//
	// ACCESSORS
	//

	@Override
	public String getInsertUri() {
		return insertUri;
	}

	@Override
	public OwnIdentity addContext(String context) {
		return (OwnIdentity) super.addContext(context);
	}

	@Override
	public OwnIdentity removeContext(String context) {
		return (OwnIdentity) super.removeContext(context);
	}

	@Override
	public OwnIdentity setProperty(String name, String value) {
		return (OwnIdentity) super.setProperty(name, value);
	}

	@Override
	public OwnIdentity removeProperty(String name) {
		return (OwnIdentity) super.removeProperty(name);
	}

	//
	// OBJECT METHODS
	//

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return super.equals(object);
	}

}
