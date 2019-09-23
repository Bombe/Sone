/*
 * Sone - IdentityLoader.java - Copyright © 2013–2019 David Roden
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

import java.util.*;
import java.util.logging.*;
import javax.annotation.*;

import com.google.common.base.*;
import com.google.inject.*;
import net.pterodactylus.sone.freenet.plugin.*;

import static java.util.concurrent.TimeUnit.*;

/**
 * Loads {@link OwnIdentity}s and the {@link Identity}s they trust.
 */
public class IdentityLoader {

	private final Logger logger = Logger.getLogger(IdentityLoader.class.getName());
	private final WebOfTrustConnector webOfTrustConnector;
	@Nullable
	private final Context context;

	public IdentityLoader(WebOfTrustConnector webOfTrustConnector) {
		this(webOfTrustConnector, null);
	}

	@Inject
	public IdentityLoader(WebOfTrustConnector webOfTrustConnector, @Nullable Context context) {
		this.webOfTrustConnector = webOfTrustConnector;
		this.context = context;
	}

	public Map<OwnIdentity, Collection<Identity>> loadIdentities() throws WebOfTrustException {
		Stopwatch stopwatch = Stopwatch.createStarted();
		Collection<OwnIdentity> currentOwnIdentities = webOfTrustConnector.loadAllOwnIdentities();
		logger.fine("Loaded " + currentOwnIdentities.size() + " own identities in " + (stopwatch.elapsed(MILLISECONDS) / 1000.0) + "s.");
		return loadTrustedIdentitiesForOwnIdentities(currentOwnIdentities);
	}

	private Map<OwnIdentity, Collection<Identity>> loadTrustedIdentitiesForOwnIdentities(Collection<OwnIdentity> ownIdentities) throws PluginException {
		Map<OwnIdentity, Collection<Identity>> currentIdentities = new HashMap<>();

		for (OwnIdentity ownIdentity : ownIdentities) {
			if (identityDoesNotHaveTheCorrectContext(ownIdentity)) {
				currentIdentities.put(ownIdentity, Collections.<Identity>emptySet());
				continue;
			}

			Stopwatch stopwatch = Stopwatch.createStarted();
			Set<Identity> trustedIdentities = webOfTrustConnector.loadTrustedIdentities(ownIdentity, (context == null) ? null : context.getContext());
			logger.fine("Loaded " + trustedIdentities.size() + " identities for " + ownIdentity.getNickname() + " in " + (stopwatch.elapsed(MILLISECONDS) / 1000.0) + "s.");
			currentIdentities.put(ownIdentity, trustedIdentities);
		}

		return currentIdentities;
	}

	private boolean identityDoesNotHaveTheCorrectContext(OwnIdentity ownIdentity) {
		return (context != null) && !ownIdentity.hasContext(context.getContext());
	}

}
