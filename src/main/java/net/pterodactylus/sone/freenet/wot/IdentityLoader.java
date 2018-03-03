/*
 * Sone - IdentityLoader.java - Copyright © 2013–2016 David Roden
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

import static java.util.Collections.emptySet;
import static net.pterodactylus.sone.freenet.wot.Context.extractContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.pterodactylus.sone.freenet.plugin.PluginException;

import com.google.common.base.Optional;
import com.google.inject.Inject;

/**
 * Loads {@link OwnIdentity}s and the {@link Identity}s they trust.
 */
public class IdentityLoader {

	private final WebOfTrustConnector webOfTrustConnector;
	private final Optional<Context> context;

	public IdentityLoader(WebOfTrustConnector webOfTrustConnector) {
		this(webOfTrustConnector, Optional.<Context>absent());
	}

	@Inject
	public IdentityLoader(WebOfTrustConnector webOfTrustConnector, Optional<Context> context) {
		this.webOfTrustConnector = webOfTrustConnector;
		this.context = context;
	}

	public Map<OwnIdentity, Collection<Identity>> loadIdentities() throws WebOfTrustException {
		Collection<OwnIdentity> currentOwnIdentities = webOfTrustConnector.loadAllOwnIdentities();
		return loadTrustedIdentitiesForOwnIdentities(currentOwnIdentities);
	}

	private Map<OwnIdentity, Collection<Identity>> loadTrustedIdentitiesForOwnIdentities(Collection<OwnIdentity> ownIdentities) throws PluginException {
		Map<OwnIdentity, Collection<Identity>> currentIdentities = new HashMap<OwnIdentity, Collection<Identity>>();

		for (OwnIdentity ownIdentity : ownIdentities) {
			if (identityDoesNotHaveTheCorrectContext(ownIdentity)) {
				currentIdentities.put(ownIdentity, Collections.<Identity>emptySet());
				continue;
			}

			Set<Identity> trustedIdentities = webOfTrustConnector.loadTrustedIdentities(ownIdentity, context.transform(extractContext));
			currentIdentities.put(ownIdentity, trustedIdentities);
		}

		return currentIdentities;
	}

	private boolean identityDoesNotHaveTheCorrectContext(OwnIdentity ownIdentity) {
		return context.isPresent() && !ownIdentity.hasContext(context.transform(extractContext).get());
	}

}
