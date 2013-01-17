/*
 * Sone - DefaultIdentity.java - Copyright © 2010–2013 David Roden
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A Web of Trust identity.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DefaultIdentity implements Identity {

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

	/** Cached trust. */
	private final Map<OwnIdentity, Trust> trustCache = Collections.synchronizedMap(new HashMap<OwnIdentity, Trust>());

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
	public DefaultIdentity(String id, String nickname, String requestUri) {
		this.id = id;
		this.nickname = nickname;
		this.requestUri = requestUri;
	}

	//
	// ACCESSORS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getNickname() {
		return nickname;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRequestUri() {
		return requestUri;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getContexts() {
		return Collections.unmodifiableSet(contexts);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasContext(String context) {
		return contexts.contains(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setContexts(Collection<String> contexts) {
		this.contexts.clear();
		this.contexts.addAll(contexts);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addContext(String context) {
		contexts.add(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeContext(String context) {
		contexts.remove(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProperties(Map<String, String> properties) {
		this.properties.clear();
		this.properties.putAll(properties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProperty(String name) {
		return properties.get(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProperty(String name, String value) {
		properties.put(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeProperty(String name) {
		properties.remove(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Trust getTrust(OwnIdentity ownIdentity) {
		return trustCache.get(ownIdentity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTrust(OwnIdentity ownIdentity, Trust trust) {
		trustCache.put(ownIdentity, trust);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeTrust(OwnIdentity ownIdentity) {
		trustCache.remove(ownIdentity);
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
		if (!(object instanceof DefaultIdentity)) {
			return false;
		}
		DefaultIdentity identity = (DefaultIdentity) object;
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
