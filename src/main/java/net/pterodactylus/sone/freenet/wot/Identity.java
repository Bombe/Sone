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

	/** The Web of Trust connector. */
	protected final WebOfTrustConnector webOfTrustConnector;

	/** The ID of the identity. */
	private final String id;

	/** The nickname of the identity. */
	private final String nickname;

	/** The request URI of the identity. */
	private final String requestUri;

	/** The contexts of the identity. */
	protected final Set<String> contexts = Collections.synchronizedSet(new HashSet<String>());

	/** The properties of the identity. */
	private final Map<String, String> properties = Collections.synchronizedMap(new HashMap<String, String>());

	/**
	 * Creates a new identity.
	 *
	 * @param webOfTrustConnector
	 *            The Web of Trust connector
	 * @param id
	 *            The ID of the identity
	 * @param nickname
	 *            The nickname of the identity
	 * @param requestUri
	 *            The request URI of the identity
	 */
	public Identity(WebOfTrustConnector webOfTrustConnector, String id, String nickname, String requestUri) {
		this.webOfTrustConnector = webOfTrustConnector;
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
	 * Returns the contexts of the identity.
	 *
	 * @return The contexts of the identity
	 * @throws PluginException
	 *             if an error occured communicating with the Web of Trust
	 *             plugin
	 */
	public Set<String> getContexts() throws PluginException {
		return getContexts(false);
	}

	/**
	 * Returns the contexts of the identity.
	 *
	 * @param forceReload
	 *            {@code true} to force a reload of the contexts
	 * @return The contexts of the identity
	 * @throws PluginException
	 *             if an error occured communicating with the Web of Trust
	 *             plugin
	 */
	public Set<String> getContexts(boolean forceReload) throws PluginException {
		if (contexts.isEmpty() || forceReload) {
			Set<String> contexts = webOfTrustConnector.loadIdentityContexts(this);
			this.contexts.clear();
			this.contexts.addAll(contexts);
		}
		return Collections.unmodifiableSet(contexts);
	}

	/**
	 * Returns whether the identity contains the given context.
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
	 * Returns the properties of the identity.
	 *
	 * @return The properties of the identity
	 */
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	/**
	 * Returns the value of the property with the given name.
	 *
	 * @param name
	 *            The name of the property
	 * @return The value of the property, or {@code null} if there is no such
	 *         property
	 */
	public String getProperty(String name) {
		return properties.get(name);
	}

	/**
	 * Sets the property with the given name to the given value.
	 *
	 * @param name
	 *            The name of the property to set
	 * @param value
	 *            The new value of the property
	 */
	public void setProperty(String name, String value) {
		properties.put(name, value);
		/* TODO - set property. */
	}

	/**
	 * Removes the property with the given name.
	 *
	 * @param name
	 *            The name of the property to remove
	 */
	public void removeProperty(String name) {
		properties.remove(name);
		/* TODO - remove property. */
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

}
