/*
 * Sone - IdentityManagerImpl.java - Copyright © 2010–2016 David Roden
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

import static java.util.logging.Logger.getLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.freenet.plugin.PluginException;
import net.pterodactylus.util.service.AbstractService;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

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
@Singleton
public class IdentityManagerImpl extends AbstractService implements IdentityManager {

	/** The logger. */
	private static final Logger logger = getLogger(IdentityManagerImpl.class.getName());

	/** The event bus. */
	private final EventBus eventBus;

	private final IdentityLoader identityLoader;

	/** The Web of Trust connector. */
	private final WebOfTrustConnector webOfTrustConnector;

	/** The currently known own identities. */
	private final Set<OwnIdentity> currentOwnIdentities = Sets.newHashSet();

	/**
	 * Creates a new identity manager.
	 *
	 * @param eventBus
	 *            The event bus
	 * @param webOfTrustConnector
	 *            The Web of Trust connector
	 */
	@Inject
	public IdentityManagerImpl(EventBus eventBus, WebOfTrustConnector webOfTrustConnector, IdentityLoader identityLoader) {
		super("Sone Identity Manager", false);
		this.eventBus = eventBus;
		this.webOfTrustConnector = webOfTrustConnector;
		this.identityLoader = identityLoader;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns whether the Web of Trust plugin could be reached during the last
	 * try.
	 *
	 * @return {@code true} if the Web of Trust plugin is connected,
	 *         {@code false} otherwise
	 */
	@Override
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
	 * Returns all own identities.
	 *
	 * @return All own identities
	 */
	@Override
	public Set<OwnIdentity> getAllOwnIdentities() {
		synchronized (currentOwnIdentities) {
			return new HashSet<OwnIdentity>(currentOwnIdentities);
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
		Map<OwnIdentity, Collection<Identity>> oldIdentities = new HashMap<OwnIdentity, Collection<Identity>>();

		while (!shouldStop()) {
			try {
				Map<OwnIdentity, Collection<Identity>> currentIdentities = identityLoader.loadIdentities();

				IdentityChangeEventSender identityChangeEventSender = new IdentityChangeEventSender(eventBus, oldIdentities);
				identityChangeEventSender.detectChanges(currentIdentities);

				oldIdentities = currentIdentities;

				synchronized (currentOwnIdentities) {
					currentOwnIdentities.clear();
					currentOwnIdentities.addAll(currentIdentities.keySet());
				}
			} catch (WebOfTrustException wote1) {
				logger.log(Level.WARNING, "WoT has disappeared!", wote1);
			}

			/* wait 15 minutes before checking again. */
			sleep(15 * 60 * 1000);
		}
	}

}
