/*
 * Sone - IdentityLoader.java - Copyright © 2013 David Roden
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

import static com.google.common.collect.HashMultimap.create;

import java.util.Collection;
import java.util.Set;

import net.pterodactylus.sone.freenet.plugin.PluginException;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;

/**
 * Loads {@link OwnIdentity}s and the {@link Identity}s they trust.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IdentityLoader {

	private final WebOfTrustConnector webOfTrustConnector;
	private final Optional<String> context;

	public IdentityLoader(WebOfTrustConnector webOfTrustConnector) {
		this(webOfTrustConnector, Optional.<String>absent());
	}

	public IdentityLoader(WebOfTrustConnector webOfTrustConnector, Optional<String> context) {
		this.webOfTrustConnector = webOfTrustConnector;
		this.context = context;
	}

	public Multimap<OwnIdentity, Identity> loadIdentities() throws WebOfTrustException {
		Collection<OwnIdentity> currentOwnIdentities = webOfTrustConnector.loadAllOwnIdentities();
		return loadTrustedIdentitiesForOwnIdentities(currentOwnIdentities);
	}

	private Multimap<OwnIdentity, Identity> loadTrustedIdentitiesForOwnIdentities(Collection<OwnIdentity> ownIdentities) throws PluginException {
		Multimap<OwnIdentity, Identity> currentIdentities = create();

		for (OwnIdentity ownIdentity : ownIdentities) {
			if (identityDoesNotHaveTheCorrectContext(ownIdentity)) {
				continue;
			}

			Set<Identity> trustedIdentities = webOfTrustConnector.loadTrustedIdentities(ownIdentity, context.orNull());
			currentIdentities.putAll(ownIdentity, trustedIdentities);
		}

		return currentIdentities;
	}

	private boolean identityDoesNotHaveTheCorrectContext(OwnIdentity ownIdentity) {
		return context.isPresent() && !ownIdentity.hasContext(context.get());
	}

}
