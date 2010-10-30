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
	 * Returns the contexts of the identity. If the contexts have not been
	 * loaded yet, they will be loaded. If loading the contexts fails, an empty
	 * set is returned.
	 *
	 * @return The contexts of the identity
	 * @throws PluginException
	 *             if an error occured communicating with the Web of Trust
	 *             plugin
	 */
	public Set<String> getContexts() throws PluginException {
		return webOfTrustConnector.loadIdentityContexts(this);
	}

	/**
	 * Returns whether the identity contains the given context.
	 *
	 * @param context
	 *            The context to check for
	 * @return {@code true} if this identity has the given context,
	 *         {@code false} otherwise
	 * @throws PluginException
	 *             if an error occured communicating with the Web of Trust
	 *             plugin
	 */
	public boolean hasContext(String context) throws PluginException {
		return getContexts().contains(context);
	}

	/**
	 * Returns the value of the property with the given name.
	 *
	 * @param name
	 *            The name of the property
	 * @return The value of the property, or {@code null} if there is no such
	 *         property
	 * @throws PluginException
	 *             if an error occured communicating with the Web of Trust
	 *             plugin
	 */
	public String getProperty(String name) throws PluginException {
		return webOfTrustConnector.getProperty(this, name);
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
