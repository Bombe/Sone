/*
 * Sone - DefaultOwnIdentity.java - Copyright © 2010 David Roden
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
import java.util.Map.Entry;
import java.util.Set;

import net.pterodactylus.util.validation.Validation;

/**
 * An own identity is an identity that the owner of the node has full control
 * over.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DefaultOwnIdentity extends DefaultIdentity implements OwnIdentity {

	/** The identity manager. */
	private final WebOfTrustConnector webOfTrustConnector;

	/** The insert URI of the identity. */
	private final String insertUri;

	/**
	 * Creates a new own identity.
	 *
	 * @param webOfTrustConnector
	 *            The identity manager
	 * @param id
	 *            The ID of the identity
	 * @param nickname
	 *            The nickname of the identity
	 * @param requestUri
	 *            The request URI of the identity
	 * @param insertUri
	 *            The insert URI of the identity
	 */
	public DefaultOwnIdentity(WebOfTrustConnector webOfTrustConnector, String id, String nickname, String requestUri, String insertUri) {
		super(webOfTrustConnector, id, nickname, requestUri);
		this.webOfTrustConnector = webOfTrustConnector;
		this.insertUri = insertUri;
	}

	/**
	 * Copy constructor for an own identity.
	 *
	 * @param webOfTrustConnector
	 *            The web of trust connector
	 * @param ownIdentity
	 *            The own identity to copy
	 */
	public DefaultOwnIdentity(WebOfTrustConnector webOfTrustConnector, OwnIdentity ownIdentity) {
		super(webOfTrustConnector, ownIdentity.getId(), ownIdentity.getNickname(), ownIdentity.getRequestUri());
		this.webOfTrustConnector = webOfTrustConnector;
		this.insertUri = ownIdentity.getInsertUri();
		setContextsPrivate(ownIdentity.getContexts());
		setPropertiesPrivate(ownIdentity.getProperties());
	}

	//
	// ACCESSORS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getInsertUri() {
		return insertUri;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addContext(String context) throws WebOfTrustException {
		webOfTrustConnector.addContext(this, context);
		addContextPrivate(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeContext(String context) throws WebOfTrustException {
		webOfTrustConnector.removeContext(this, context);
		removeContextPrivate(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setContexts(Set<String> contexts) throws WebOfTrustException {
		for (String context : getContexts()) {
			if (!contexts.contains(context)) {
				webOfTrustConnector.removeContext(this, context);
			}
		}
		for (String context : contexts) {
			if (!getContexts().contains(context)) {
				webOfTrustConnector.addContext(this, context);
			}
		}
		setContextsPrivate(contexts);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProperty(String name, String value) throws WebOfTrustException {
		webOfTrustConnector.setProperty(this, name, value);
		setPropertyPrivate(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeProperty(String name) throws WebOfTrustException {
		webOfTrustConnector.removeProperty(this, name);
		removePropertyPrivate(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProperties(Map<String, String> properties) throws WebOfTrustException {
		for (Entry<String, String> oldProperty : getProperties().entrySet()) {
			if (!properties.containsKey(oldProperty.getKey())) {
				webOfTrustConnector.removeProperty(this, oldProperty.getKey());
			} else {
				webOfTrustConnector.setProperty(this, oldProperty.getKey(), properties.get(oldProperty.getKey()));
			}
		}
		for (Entry<String, String> newProperty : properties.entrySet()) {
			if (!getProperties().containsKey(newProperty.getKey())) {
				webOfTrustConnector.setProperty(this, newProperty.getKey(), newProperty.getValue());
			}
		}
		setPropertiesPrivate(properties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTrust(Identity target, int trustValue, String comment) throws WebOfTrustException {
		Validation.begin().isNotNull("Trust Target", target).isNotNull("Trust Comment", comment).isLessOrEqual("Trust Value", trustValue, 100).isGreaterOrEqual("Trust Value", trustValue, -100).check();
		webOfTrustConnector.setTrust(this, target, trustValue, comment);
		if (target instanceof DefaultIdentity) {
			((DefaultIdentity) target).setTrustPrivate(this, new Trust(trustValue, trustValue, 0));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeTrust(Identity target) throws WebOfTrustException {
		Validation.begin().isNotNull("Trust Target", target).check();
		webOfTrustConnector.removeTrust(this, target);
		if (target instanceof DefaultIdentity) {
			((DefaultIdentity) target).setTrustPrivate(this, new Trust(null, null, null));
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
		/* The hash of DefaultIdentity is fine. */
		return super.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		/* The ID of the superclass is still enough. */
		return super.equals(object);
	}

}
