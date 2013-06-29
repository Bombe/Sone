/*
 * Sone - IdentityManager.java - Copyright © 2010–2013 David Roden
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.freenet.plugin.PluginException;
import net.pterodactylus.sone.freenet.wot.event.IdentityAddedEvent;
import net.pterodactylus.sone.freenet.wot.event.IdentityRemovedEvent;
import net.pterodactylus.sone.freenet.wot.event.IdentityUpdatedEvent;
import net.pterodactylus.sone.freenet.wot.event.OwnIdentityAddedEvent;
import net.pterodactylus.sone.freenet.wot.event.OwnIdentityRemovedEvent;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.service.AbstractService;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * The identity manager takes care of loading and storing identities, their
 * contexts, and properties. It does so in a way that does not expose errors via
 * exceptions but it only logs them and tries to return sensible defaults.
 * <p>
 * It is also responsible for polling identities from the Web of Trust plugin
 * and sending events to the {@link EventBus} when {@link Identity}s and
 * {@link OwnIdentity}s are discovered or disappearing.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IdentityManager extends AbstractService {

	/** Object used for synchronization. */
	@SuppressWarnings("hiding")
	private final Object syncObject = new Object() {
		/* inner class for better lock names. */
	};

	/** The logger. */
	private static final Logger logger = Logging.getLogger(IdentityManager.class);

	/** The event bus. */
	private final EventBus eventBus;

	/** The Web of Trust connector. */
	private final WebOfTrustConnector webOfTrustConnector;

	/** The context to filter for. */
	private final String context;

	/** The currently known own identities. */
	/* synchronize access on syncObject. */
	private final Map<String, OwnIdentity> currentOwnIdentities = new HashMap<String, OwnIdentity>();

	/** The last time all identities were loaded. */
	private volatile long identitiesLastLoaded;

	/**
	 * Creates a new identity manager.
	 *
	 * @param eventBus
	 *            The event bus
	 * @param webOfTrustConnector
	 *            The Web of Trust connector
	 * @param context
	 *            The context to focus on (may be {@code null} to ignore
	 *            contexts)
	 */
	@Inject
	public IdentityManager(EventBus eventBus, WebOfTrustConnector webOfTrustConnector, @Named("WebOfTrustContext") String context) {
		super("Sone Identity Manager", false);
		this.eventBus = eventBus;
		this.webOfTrustConnector = webOfTrustConnector;
		this.context = context;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the last time all identities were loaded.
	 *
	 * @return The last time all identities were loaded (in milliseconds since
	 *         Jan 1, 1970 UTC)
	 */
	public long getIdentitiesLastLoaded() {
		return identitiesLastLoaded;
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
				return new DefaultOwnIdentity(ownIdentity);
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
		return new HashSet<OwnIdentity>(currentOwnIdentities.values());
	}

	//
	// SERVICE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void serviceRun() {
		Map<OwnIdentity, Map<String, Identity>> oldIdentities = Collections.emptyMap();
		while (!shouldStop()) {
			Map<OwnIdentity, Map<String, Identity>> currentIdentities = new HashMap<OwnIdentity, Map<String, Identity>>();
			Map<String, OwnIdentity> currentOwnIdentities = new HashMap<String, OwnIdentity>();

			Set<OwnIdentity> ownIdentities = null;
			boolean identitiesLoaded = false;
			try {
				/* get all identities with the wanted context from WoT. */
				ownIdentities = webOfTrustConnector.loadAllOwnIdentities();

				/* load trusted identities. */
				for (OwnIdentity ownIdentity : ownIdentities) {
					currentOwnIdentities.put(ownIdentity.getId(), ownIdentity);
					Map<String, Identity> identities = new HashMap<String, Identity>();
					currentIdentities.put(ownIdentity, identities);

					/*
					 * if the context doesn’t match, skip getting trusted
					 * identities.
					 */
					if ((context != null) && !ownIdentity.hasContext(context)) {
						continue;
					}

					/* load trusted identities. */
					Set<Identity> trustedIdentities = webOfTrustConnector.loadTrustedIdentities(ownIdentity, context);
					for (Identity identity : trustedIdentities) {
						identities.put(identity.getId(), identity);
					}
				}
				identitiesLoaded = true;
				identitiesLastLoaded = System.currentTimeMillis();
			} catch (WebOfTrustException wote1) {
				logger.log(Level.WARNING, "WoT has disappeared!", wote1);
			}

			if (identitiesLoaded) {

				/* check for changes. */
				checkOwnIdentities(currentOwnIdentities);

				/* now check for changes in remote identities. */
				for (OwnIdentity ownIdentity : currentOwnIdentities.values()) {

					/* find new identities. */
					for (Identity currentIdentity : currentIdentities.get(ownIdentity).values()) {
						if (!oldIdentities.containsKey(ownIdentity) || !oldIdentities.get(ownIdentity).containsKey(currentIdentity.getId())) {
							eventBus.post(new IdentityAddedEvent(ownIdentity, currentIdentity));
						}
					}

					/* find removed identities. */
					if (oldIdentities.containsKey(ownIdentity)) {
						for (Identity oldIdentity : oldIdentities.get(ownIdentity).values()) {
							if (!currentIdentities.get(ownIdentity).containsKey(oldIdentity.getId())) {
								eventBus.post(new IdentityRemovedEvent(ownIdentity, oldIdentity));
							}
						}

						/* check for changes in the contexts. */
						for (Identity oldIdentity : oldIdentities.get(ownIdentity).values()) {
							if (!currentIdentities.get(ownIdentity).containsKey(oldIdentity.getId())) {
								continue;
							}
							Identity newIdentity = currentIdentities.get(ownIdentity).get(oldIdentity.getId());
							Set<String> oldContexts = oldIdentity.getContexts();
							Set<String> newContexts = newIdentity.getContexts();
							if (oldContexts.size() != newContexts.size()) {
								eventBus.post(new IdentityUpdatedEvent(ownIdentity, newIdentity));
								continue;
							}
							for (String oldContext : oldContexts) {
								if (!newContexts.contains(oldContext)) {
									eventBus.post(new IdentityUpdatedEvent(ownIdentity, newIdentity));
									break;
								}
							}
						}

						/* check for changes in the properties. */
						for (Identity oldIdentity : oldIdentities.get(ownIdentity).values()) {
							if (!currentIdentities.get(ownIdentity).containsKey(oldIdentity.getId())) {
								continue;
							}
							Identity newIdentity = currentIdentities.get(ownIdentity).get(oldIdentity.getId());
							Map<String, String> oldProperties = oldIdentity.getProperties();
							Map<String, String> newProperties = newIdentity.getProperties();
							if (oldProperties.size() != newProperties.size()) {
								eventBus.post(new IdentityUpdatedEvent(ownIdentity, newIdentity));
								continue;
							}
							for (Entry<String, String> oldProperty : oldProperties.entrySet()) {
								if (!newProperties.containsKey(oldProperty.getKey()) || !newProperties.get(oldProperty.getKey()).equals(oldProperty.getValue())) {
									eventBus.post(new IdentityUpdatedEvent(ownIdentity, newIdentity));
									break;
								}
							}
						}
					}
				}

				/* remember the current set of identities. */
				oldIdentities = currentIdentities;
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
				OwnIdentity newOwnIdentity = newOwnIdentities.get(oldOwnIdentity.getId());
				if ((newOwnIdentity == null) || ((context != null) && oldOwnIdentity.hasContext(context) && !newOwnIdentity.hasContext(context))) {
					eventBus.post(new OwnIdentityRemovedEvent(new DefaultOwnIdentity(oldOwnIdentity)));
				}
			}

			/* find added own identities. */
			for (OwnIdentity currentOwnIdentity : newOwnIdentities.values()) {
				OwnIdentity oldOwnIdentity = currentOwnIdentities.get(currentOwnIdentity.getId());
				if (((oldOwnIdentity == null) && ((context == null) || currentOwnIdentity.hasContext(context))) || ((oldOwnIdentity != null) && (context != null) && (!oldOwnIdentity.hasContext(context) && currentOwnIdentity.hasContext(context)))) {
					eventBus.post(new OwnIdentityAddedEvent(new DefaultOwnIdentity(currentOwnIdentity)));
				}
			}

			currentOwnIdentities.clear();
			currentOwnIdentities.putAll(newOwnIdentities);
		}
	}

}
