/*
 * Sone - IdentityManager.java - Copyright © 2010 David Roden
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.service.AbstractService;

/**
 * The identity manager takes care of loading and storing identities, their
 * contexts, and properties. It does so in a way that does not expose errors via
 * exceptions but it only logs them and tries to return sensible defaults.
 * <p>
 * It is also responsible for polling identities from the Web of Trust plugin
 * and notifying registered {@link IdentityListener}s when {@link Identity}s and
 * {@link OwnIdentity}s are discovered or disappearing.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IdentityManager extends AbstractService {

	/** Object used for synchronization. */
	private final Object syncObject = new Object() {
		/* inner class for better lock names. */
	};

	/** The logger. */
	private static final Logger logger = Logging.getLogger(IdentityManager.class);

	/** The event manager. */
	private final IdentityListenerManager identityListenerManager = new IdentityListenerManager();

	/** The Web of Trust connector. */
	private final WebOfTrustConnector webOfTrustConnector;

	/** The context to filter for. */
	private volatile String context;

	/** The currently known own identities. */
	/* synchronize access on syncObject. */
	private Map<String, OwnIdentity> currentOwnIdentities = new HashMap<String, OwnIdentity>();

	/**
	 * Creates a new identity manager.
	 *
	 * @param webOfTrustConnector
	 *            The Web of Trust connector
	 */
	public IdentityManager(WebOfTrustConnector webOfTrustConnector) {
		super("Sone Identity Manager", false);
		this.webOfTrustConnector = webOfTrustConnector;
	}

	//
	// LISTENER MANAGEMENT
	//

	/**
	 * Adds a listener for identity events.
	 *
	 * @param identityListener
	 *            The listener to add
	 */
	public void addIdentityListener(IdentityListener identityListener) {
		identityListenerManager.addListener(identityListener);
	}

	/**
	 * Removes a listener for identity events.
	 *
	 * @param identityListener
	 *            The listener to remove
	 */
	public void removeIdentityListener(IdentityListener identityListener) {
		identityListenerManager.removeListener(identityListener);
	}

	//
	// ACCESSORS
	//

	/**
	 * Sets the context to filter own identities and trusted identities for.
	 *
	 * @param context
	 *            The context to filter for, or {@code null} to not filter
	 */
	public void setContext(String context) {
		this.context = context;
	}

	/**
	 * Returns whether the Web of Trust plugin could be reached during the last
	 * try.
	 *
	 * @return {@code true} if the Web of Trust plugin is connected,
	 *         {@code false} otherwise
	 */
	public boolean isConnected() {
		try {
			webOfTrustConnector.ping();
			return true;
		} catch (PluginException pe1) {
			/* not connected, ignore. */
			return false;
		}
	}

	/**
	 * Returns the own identity with the given ID.
	 *
	 * @param id
	 *            The ID of the own identity
	 * @return The own identity, or {@code null} if there is no such identity
	 */
	public OwnIdentity getOwnIdentity(String id) {
		Set<OwnIdentity> allOwnIdentities = getAllOwnIdentities();
		for (OwnIdentity ownIdentity : allOwnIdentities) {
			if (ownIdentity.getId().equals(id)) {
				return ownIdentity;
			}
		}
		return null;
	}

	/**
	 * Returns all own identities.
	 *
	 * @return All own identities
	 */
	public Set<OwnIdentity> getAllOwnIdentities() {
		try {
			Set<OwnIdentity> ownIdentities = webOfTrustConnector.loadAllOwnIdentities();
			Map<String, OwnIdentity> newOwnIdentities = new HashMap<String, OwnIdentity>();
			for (OwnIdentity ownIdentity : ownIdentities) {
				newOwnIdentities.put(ownIdentity.getId(), ownIdentity);
			}
			checkOwnIdentities(newOwnIdentities);
			return ownIdentities;
		} catch (PluginException pe1) {
			logger.log(Level.WARNING, "Could not load all own identities!", pe1);
			return Collections.emptySet();
		}
	}

	//
	// ACTIONS
	//

	/**
	 * Adds a context to the given own identity.
	 *
	 * @param ownIdentity
	 *            The own identity
	 * @param context
	 *            The context to add
	 */
	public void addContext(OwnIdentity ownIdentity, String context) {
		if (ownIdentity.hasContext(context)) {
			return;
		}
		try {
			webOfTrustConnector.addContext(ownIdentity, context);
			ownIdentity.addContext(context);
		} catch (PluginException pe1) {
			logger.log(Level.WARNING, "Could not add context " + context + " to OwnIdentity " + ownIdentity + ".", pe1);
		}
	}

	/**
	 * Removes a context from the given own identity.
	 *
	 * @param ownIdentity
	 *            The own identity
	 * @param context
	 *            The context to remove
	 */
	public void removeContext(OwnIdentity ownIdentity, String context) {
		if (!ownIdentity.hasContext(context)) {
			return;
		}
		try {
			webOfTrustConnector.removeContext(ownIdentity, context);
			ownIdentity.removeContext(context);
		} catch (PluginException pe1) {
			logger.log(Level.WARNING, "Could not remove context " + context + " from OwnIdentity " + ownIdentity + ".", pe1);
		}
	}

	/**
	 * Sets the property with the given name to the given value.
	 *
	 * @param ownIdentity
	 *            The own identity
	 * @param name
	 *            The name of the property
	 * @param value
	 *            The value of the property
	 */
	public void setProperty(OwnIdentity ownIdentity, String name, String value) {
		try {
			webOfTrustConnector.setProperty(ownIdentity, name, value);
			ownIdentity.setProperty(name, value);
		} catch (PluginException pe1) {
			logger.log(Level.WARNING, "Could not set property “" + name + "” to “" + value + "” for OwnIdentity: " + ownIdentity, pe1);
		}
	}

	/**
	 * Removes the property with the given name.
	 *
	 * @param ownIdentity
	 *            The own identity
	 * @param name
	 *            The name of the property to remove
	 */
	public void removeProperty(OwnIdentity ownIdentity, String name) {
		try {
			webOfTrustConnector.removeProperty(ownIdentity, name);
			ownIdentity.removeProperty(name);
		} catch (PluginException pe1) {
			logger.log(Level.WARNING, "Could not remove property “" + name + "” from OwnIdentity: " + ownIdentity, pe1);
		}
	}

	//
	// SERVICE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void serviceRun() {
		Map<String, Identity> oldIdentities = Collections.emptyMap();
		while (!shouldStop()) {
			Map<String, Identity> currentIdentities = new HashMap<String, Identity>();
			Map<String, OwnIdentity> currentOwnIdentities = new HashMap<String, OwnIdentity>();

			/* get all identities with the wanted context from WoT. */
			Set<OwnIdentity> ownIdentities;
			try {
				ownIdentities = webOfTrustConnector.loadAllOwnIdentities();
				for (OwnIdentity ownIdentity : ownIdentities) {
					if ((context != null) && !ownIdentity.hasContext(context)) {
						continue;
					}
					currentOwnIdentities.put(ownIdentity.getId(), ownIdentity);
					for (Identity identity : webOfTrustConnector.loadTrustedIdentities(ownIdentity, context)) {
						currentIdentities.put(identity.getId(), identity);
					}
				}

				checkOwnIdentities(currentOwnIdentities);

				/* find removed identities. */
				for (Identity oldIdentity : oldIdentities.values()) {
					if (!currentIdentities.containsKey(oldIdentity.getId())) {
						identityListenerManager.fireIdentityRemoved(oldIdentity);
					}
				}

				/* find new identities. */
				for (Identity currentIdentity : currentIdentities.values()) {
					if (!oldIdentities.containsKey(currentIdentity.getId())) {
						identityListenerManager.fireIdentityAdded(currentIdentity);
					}
				}

				/* check for changes in the contexts. */
				for (Identity oldIdentity : oldIdentities.values()) {
					if (!currentIdentities.containsKey(oldIdentity.getId())) {
						continue;
					}
					Identity newIdentity = currentIdentities.get(oldIdentity.getId());
					Set<String> oldContexts = oldIdentity.getContexts();
					Set<String> newContexts = newIdentity.getContexts();
					if (oldContexts.size() != newContexts.size()) {
						identityListenerManager.fireIdentityUpdated(newIdentity);
						continue;
					}
					for (String oldContext : oldContexts) {
						if (!newContexts.contains(oldContext)) {
							identityListenerManager.fireIdentityUpdated(newIdentity);
							break;
						}
					}
				}

				/* check for changes in the properties. */
				for (Identity oldIdentity : oldIdentities.values()) {
					if (!currentIdentities.containsKey(oldIdentity.getId())) {
						continue;
					}
					Identity newIdentity = currentIdentities.get(oldIdentity.getId());
					Map<String, String> oldProperties = oldIdentity.getProperties();
					Map<String, String> newProperties = newIdentity.getProperties();
					if (oldProperties.size() != newProperties.size()) {
						identityListenerManager.fireIdentityUpdated(newIdentity);
						continue;
					}
					for (Entry<String, String> oldProperty : oldProperties.entrySet()) {
						if (!newProperties.containsKey(oldProperty.getKey()) || !newProperties.get(oldProperty.getKey()).equals(oldProperty.getValue())) {
							identityListenerManager.fireIdentityUpdated(newIdentity);
							break;
						}
					}
				}

				/* remember the current set of identities. */
				oldIdentities = currentIdentities;

			} catch (PluginException pe1) {
				logger.log(Level.WARNING, "WoT has disappeared!", pe1);
			}

			/* wait a minute before checking again. */
			sleep(60 * 1000);
		}
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Checks the given new list of own identities for added or removed own
	 * identities, as compared to {@link #currentOwnIdentities}.
	 *
	 * @param newOwnIdentities
	 *            The new own identities
	 */
	private void checkOwnIdentities(Map<String, OwnIdentity> newOwnIdentities) {
		synchronized (syncObject) {

			/* find removed own identities: */
			for (OwnIdentity oldOwnIdentity : currentOwnIdentities.values()) {
				if (!newOwnIdentities.containsKey(oldOwnIdentity.getId())) {
					identityListenerManager.fireOwnIdentityRemoved(oldOwnIdentity);
				}
			}

			/* find added own identities. */
			for (OwnIdentity currentOwnIdentity : newOwnIdentities.values()) {
				if (!currentOwnIdentities.containsKey(currentOwnIdentity.getId())) {
					identityListenerManager.fireOwnIdentityAdded(currentOwnIdentity);
				}
			}

			currentOwnIdentities.clear();
			currentOwnIdentities.putAll(newOwnIdentities);
		}
	}

}
