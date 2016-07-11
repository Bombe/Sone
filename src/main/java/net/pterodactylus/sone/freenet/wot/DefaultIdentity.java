/*
 * Sone - DefaultIdentity.java - Copyright © 2010–2016 David Roden
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

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getNickname() {
		return nickname;
	}

	@Override
	public String getRequestUri() {
		return requestUri;
	}

	@Override
	public Set<String> getContexts() {
		return Collections.unmodifiableSet(contexts);
	}

	@Override
	public boolean hasContext(String context) {
		return contexts.contains(context);
	}

	@Override
	public void setContexts(Collection<String> contexts) {
		this.contexts.clear();
		this.contexts.addAll(contexts);
	}

	@Override
	public Identity addContext(String context) {
		contexts.add(context);
		return this;
	}

	@Override
	public Identity removeContext(String context) {
		contexts.remove(context);
		return this;
	}

	@Override
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		this.properties.clear();
		this.properties.putAll(properties);
	}

	@Override
	public String getProperty(String name) {
		return properties.get(name);
	}

	@Override
	public Identity setProperty(String name, String value) {
		properties.put(name, value);
		return this;
	}

	@Override
	public Identity removeProperty(String name) {
		properties.remove(name);
		return this;
	}

	@Override
	public Trust getTrust(OwnIdentity ownIdentity) {
		return trustCache.get(ownIdentity);
	}

	@Override
	public Identity setTrust(OwnIdentity ownIdentity, Trust trust) {
		trustCache.put(ownIdentity, trust);
		return this;
	}

	@Override
	public Identity removeTrust(OwnIdentity ownIdentity) {
		trustCache.remove(ownIdentity);
		return this;
	}

	//
	// OBJECT METHODS
	//

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Identity)) {
			return false;
		}
		Identity identity = (Identity) object;
		return identity.getId().equals(getId());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[id=" + id + ",nickname=" + nickname + ",contexts=" + contexts + ",properties=" + properties + "]";
	}

}
