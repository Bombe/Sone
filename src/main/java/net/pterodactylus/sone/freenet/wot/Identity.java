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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A Web of Trust identity.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Identity {

	/** The ID of the identity. */
	private final String id;

	/** The nickname of the identity. */
	private final String nickname;

	/** The request URI of the identity. */
	private final String requestUri;

	/** The contexts of the identity. */
	private final Set<String> contexts = Collections.synchronizedSet(new HashSet<String>());

	/** The properties of the identity. */
	private final Map<String, String> properties = Collections.synchronizedMap(new HashMap<String, String>());

	/**
	 * Creates a new identity.
	 *
	 * @param id
	 *            The ID of the identity
	 * @param nickname
	 *            The nickname of the identity
	 * @param requestUri
	 *            The request URI of the identity
	 */
	public Identity(String id, String nickname, String requestUri) {
		this.id = id;
		this.nickname = nickname;
		this.requestUri = requestUri;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the ID of the identity.
	 *
	 * @return The ID of the identity
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the nickname of the identity.
	 *
	 * @return The nickname of the identity
	 */
	public String getNickname() {
		return nickname;
	}

	/**
	 * Returns the request URI of the identity.
	 *
	 * @return The request URI of the identity
	 */
	public String getRequestUri() {
		return requestUri;
	}

	/**
	 * Returns all contexts of this identity.
	 *
	 * @return All contexts of this identity
	 */
	public Set<String> getContexts() {
		return Collections.unmodifiableSet(contexts);
	}

	/**
	 * Sets all contexts of this identity.
	 * <p>
	 * This method is only called by the {@link IdentityManager}.
	 *
	 * @param contexts
	 *            All contexts of the identity
	 */
	void setContexts(Set<String> contexts) {
		this.contexts.clear();
		this.contexts.addAll(contexts);
	}

	/**
	 * Returns whether this identity has the given context.
	 *
	 * @param context
	 *            The context to check for
	 * @return {@code true} if this identity has the given context,
	 *         {@code false} otherwise
	 */
	public boolean hasContext(String context) {
		return contexts.contains(context);
	}

	/**
	 * Adds the given context to this identity.
	 * <p>
	 * This method is only called by the {@link IdentityManager}.
	 *
	 * @param context
	 *            The context to add
	 */
	void addContext(String context) {
		contexts.add(context);
	}

	/**
	 * Removes the given context from this identity.
	 * <p>
	 * This method is only called by the {@link IdentityManager}.
	 *
	 * @param context
	 *            The context to remove
	 */
	void removeContext(String context) {
		contexts.remove(context);
	}

	/**
	 * Returns all properties of this identity.
	 *
	 * @return All properties of this identity
	 */
	public Map<String, String> getProperties() {
		synchronized (properties) {
			return Collections.unmodifiableMap(properties);
		}
	}

	/**
	 * Sets all properties of this identity.
	 * <p>
	 * This method is only called by the {@link IdentityManager}.
	 *
	 * @param properties
	 *            The new properties of this identity
	 */
	void setProperties(Map<String, String> properties) {
		synchronized (this.properties) {
			this.properties.clear();
			this.properties.putAll(properties);
		}
	}

	/**
	 * Sets the property with the given name to the given value.
	 * <p>
	 * This method is only called by the {@link IdentityManager}.
	 *
	 * @param name
	 *            The name of the property
	 * @param value
	 *            The value of the property
	 */
	void setProperty(String name, String value) {
		synchronized (properties) {
			properties.put(name, value);
		}
	}

	/**
	 * Returns the value of the property with the given name.
	 *
	 * @param name
	 *            The name of the property
	 * @return The value of the property
	 */
	public String getProperty(String name) {
		synchronized (properties) {
			return properties.get(name);
		}
	}

	/**
	 * Removes the property with the given name.
	 * <p>
	 * This method is only called by the {@link IdentityManager}.
	 *
	 * @param name
	 *            The name of the property to remove
	 */
	void removeProperty(String name) {
		synchronized (properties) {
			properties.remove(name);
		}
	}

	//
	// OBJECT METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Identity)) {
			return false;
		}
		Identity identity = (Identity) object;
		return identity.id.equals(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[id=" + id + ",nickname=" + nickname + ",contexts=" + contexts + ",properties=" + properties + "]";
	}

}
