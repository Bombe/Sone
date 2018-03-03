/*
 * Sone - Identity.java - Copyright © 2010–2016 David Roden
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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;

/**
 * Interface for web of trust identities, defining all functions that can be
 * performed on an identity. An identity is only a container for identity data
 * and will not perform any updating in the WebOfTrust plugin itself.
 */
public interface Identity {

	public static final Function<Identity, Set<String>> TO_CONTEXTS = new Function<Identity, Set<String>>() {
		@Override
		public Set<String> apply(Identity identity) {
			return (identity == null) ? Collections.<String>emptySet() : identity.getContexts();
		}
	};

	public static final Function<Identity, Map<String, String>> TO_PROPERTIES = new Function<Identity, Map<String, String>>() {
		@Override
		public Map<String, String> apply(Identity input) {
			return (input == null) ? Collections.<String, String>emptyMap() : input.getProperties();
		}
	};

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
	 * Adds the given context to this identity.
	 *
	 * @param context
	 *            The context to add
	 */
	public Identity addContext(String context);

	/**
	 * Sets all contexts of this identity.
	 *
	 * @param contexts
	 *            All contexts of the identity
	 */
	public void setContexts(Collection<String> contexts);

	/**
	 * Removes the given context from this identity.
	 *
	 * @param context
	 *            The context to remove
	 */
	public Identity removeContext(String context);

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
	 * Sets the property with the given name to the given value.
	 *
	 * @param name
	 *            The name of the property
	 * @param value
	 *            The value of the property
	 */
	public Identity setProperty(String name, String value);

	/**
	 * Sets all properties of this identity.
	 *
	 * @param properties
	 *            The new properties of this identity
	 */
	public void setProperties(Map<String, String> properties);

	/**
	 * Removes the property with the given name.
	 *
	 * @param name
	 *            The name of the property to remove
	 */
	public Identity removeProperty(String name);

	/**
	 * Retrieves the trust that this identity receives from the given own
	 * identity. If this identity is not in the own identity’s trust tree, a
	 * {@link Trust} is returned that has all its elements set to {@code null}.
	 * If the trust can not be retrieved, {@code null} is returned.
	 *
	 * @param ownIdentity
	 *            The own identity to get the trust for
	 * @return The trust assigned to this identity, or {@code null} if the trust
	 *         could not be retrieved
	 */
	public Trust getTrust(OwnIdentity ownIdentity);

	/**
	 * Sets the trust given by an own identity to this identity.
	 *
	 * @param ownIdentity
	 *            The own identity that gave trust to this identity
	 * @param trust
	 *            The trust given by the given own identity
	 */
	public Identity setTrust(OwnIdentity ownIdentity, Trust trust);

	/**
	 * Removes trust assignment from the given own identity for this identity.
	 *
	 * @param ownIdentity
	 *            The own identity that removed the trust assignment for this
	 *            identity
	 */
	public Identity removeTrust(OwnIdentity ownIdentity);

}
