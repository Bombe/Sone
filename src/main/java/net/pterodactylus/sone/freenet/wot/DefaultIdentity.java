/*
 * Sone - DefaultIdentity.java - Copyright © 2010–2012 David Roden
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
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.freenet.plugin.PluginException;
import net.pterodactylus.util.cache.CacheException;
import net.pterodactylus.util.cache.CacheItem;
import net.pterodactylus.util.cache.DefaultCacheItem;
import net.pterodactylus.util.cache.MemoryCache;
import net.pterodactylus.util.cache.ValueRetriever;
import net.pterodactylus.util.cache.WritableCache;
import net.pterodactylus.util.collection.TimedMap;
import net.pterodactylus.util.logging.Logging;

/**
 * A Web of Trust identity.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DefaultIdentity implements Identity {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(DefaultIdentity.class);

	/** The web of trust connector. */
	private final WebOfTrustConnector webOfTrustConnector;

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
	/* synchronize on itself. */
	private final WritableCache<OwnIdentity, Trust> trustCache = new MemoryCache<OwnIdentity, Trust>(new ValueRetriever<OwnIdentity, Trust>() {

		@Override
		@SuppressWarnings("synthetic-access")
		public CacheItem<Trust> retrieve(OwnIdentity ownIdentity) throws CacheException {
			try {
				return new DefaultCacheItem<Trust>(webOfTrustConnector.getTrust(ownIdentity, DefaultIdentity.this));
			} catch (PluginException pe1) {
				throw new CacheException("Could not retrieve trust for OwnIdentity: " + ownIdentity, pe1);
			}
		}

	}, new TimedMap<OwnIdentity, CacheItem<Trust>>(60 * 60 * 1000));

	/**
	 * Creates a new identity.
	 *
	 * @param webOfTrustConnector
	 *            The web of trust connector
	 * @param id
	 *            The ID of the identity
	 * @param nickname
	 *            The nickname of the identity
	 * @param requestUri
	 *            The request URI of the identity
	 */
	public DefaultIdentity(WebOfTrustConnector webOfTrustConnector, String id, String nickname, String requestUri) {
		this.webOfTrustConnector = webOfTrustConnector;
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
	 * Sets the contexts of this identity.
	 * <p>
	 * This method is only called by the {@link IdentityManager}.
	 *
	 * @param contexts
	 *            The contexts to set
	 */
	void setContextsPrivate(Set<String> contexts) {
		this.contexts.clear();
		this.contexts.addAll(contexts);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	void addContextPrivate(String context) {
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
	public void removeContextPrivate(String context) {
		contexts.remove(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	void setPropertiesPrivate(Map<String, String> properties) {
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
	void setPropertyPrivate(String name, String value) {
		synchronized (properties) {
			properties.put(name, value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	void removePropertyPrivate(String name) {
		synchronized (properties) {
			properties.remove(name);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Trust getTrust(OwnIdentity ownIdentity) {
		try {
			synchronized (trustCache) {
				return trustCache.get(ownIdentity);
			}
		} catch (CacheException ce1) {
			logger.log(Level.WARNING, String.format("Could not get trust for OwnIdentity: %s", ownIdentity), ce1);
			return null;
		}
	}

	/**
	 * Sets the trust received for this identity by the given own identity.
	 *
	 * @param ownIdentity
	 *            The own identity that gives the trust
	 * @param trust
	 *            The trust received for this identity
	 */
	void setTrustPrivate(OwnIdentity ownIdentity, Trust trust) {
		synchronized (trustCache) {
			trustCache.put(ownIdentity, trust);
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
