/*
 * Sone - Identity.java - Copyright © 2010 David Roden
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

import java.util.Map;
import java.util.Set;

/**
 * Interface for web of trust identities, defining all functions that can be
 * performed on an identity. The identity is the main entry point for identity
 * management.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface Identity {

	/**
	 * Returns the ID of the identity.
	 *
	 * @return The ID of the identity
	 */
	public String getId();

	/**
	 * Returns the nickname of the identity.
	 *
	 * @return The nickname of the identity
	 */
	public String getNickname();

	/**
	 * Returns the request URI of the identity.
	 *
	 * @return The request URI of the identity
	 */
	public String getRequestUri();

	/**
	 * Returns all contexts of this identity.
	 *
	 * @return All contexts of this identity
	 */
	public Set<String> getContexts();

	/**
	 * Returns whether this identity has the given context.
	 *
	 * @param context
	 *            The context to check for
	 * @return {@code true} if this identity has the given context,
	 *         {@code false} otherwise
	 */
	public boolean hasContext(String context);

	/**
	 * Returns all properties of this identity.
	 *
	 * @return All properties of this identity
	 */
	public Map<String, String> getProperties();

	/**
	 * Returns the value of the property with the given name.
	 *
	 * @param name
	 *            The name of the property
	 * @return The value of the property
	 */
	public String getProperty(String name);

	/**
	 * Retrieves the trust that this identity receives from the given own
	 * identity.
	 *
	 * @param ownIdentity
	 *            The own identity to get the trust for
	 * @return The trust assigned to this identity
	 * @throws WebOfTrustException
	 *             if an error occurs retrieving the trust
	 */
	public Trust getTrust(OwnIdentity ownIdentity) throws WebOfTrustException;

}
