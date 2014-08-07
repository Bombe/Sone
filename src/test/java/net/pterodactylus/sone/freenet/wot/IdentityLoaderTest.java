/*
 * Sone - IdentityLoaderTest.java - Copyright © 2013 David Roden
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

import static com.google.common.base.Optional.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link IdentityLoader}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IdentityLoaderTest {

	private final WebOfTrustConnector webOfTrustConnector = mock(WebOfTrustConnector.class);
	private final IdentityLoader identityLoader = new IdentityLoader(webOfTrustConnector, of(new Context("Test")));
	private final IdentityLoader identityLoaderWithoutContext = new IdentityLoader(webOfTrustConnector);

	@Before
	public void setup() throws WebOfTrustException {
		List<OwnIdentity> ownIdentities = createOwnIdentities();
		when(webOfTrustConnector.loadAllOwnIdentities()).thenReturn(newHashSet(ownIdentities));
		when(webOfTrustConnector.loadTrustedIdentities(eq(ownIdentities.get(0)), any(Optional.class))).thenReturn(createTrustedIdentitiesForFirstOwnIdentity());
		when(webOfTrustConnector.loadTrustedIdentities(eq(ownIdentities.get(1)), any(Optional.class))).thenReturn(createTrustedIdentitiesForSecondOwnIdentity());
		when(webOfTrustConnector.loadTrustedIdentities(eq(ownIdentities.get(2)), any(Optional.class))).thenReturn(createTrustedIdentitiesForThirdOwnIdentity());
		when(webOfTrustConnector.loadTrustedIdentities(eq(ownIdentities.get(3)), any(Optional.class))).thenReturn(createTrustedIdentitiesForFourthOwnIdentity());
	}

	private List<OwnIdentity> createOwnIdentities() {
		return newArrayList(
				createOwnIdentity("O1", "ON1", "OR1", "OI1", asList("Test", "Test2"), ImmutableMap.of("KeyA", "ValueA", "KeyB", "ValueB")),
				createOwnIdentity("O2", "ON2", "OR2", "OI2", asList("Test"), ImmutableMap.of("KeyC", "ValueC")),
				createOwnIdentity("O3", "ON3", "OR3", "OI3", asList("Test2"), ImmutableMap.of("KeyE", "ValueE", "KeyD", "ValueD")),
				createOwnIdentity("O4", "ON4", "OR$", "OI4", asList("Test"), ImmutableMap.of("KeyA", "ValueA", "KeyD", "ValueD"))
		);
	}

	private Set<Identity> createTrustedIdentitiesForFirstOwnIdentity() {
		return newHashSet(
				createIdentity("I11", "IN11", "IR11", asList("Test"), ImmutableMap.of("KeyA", "ValueA"))
		);
	}

	private Set<Identity> createTrustedIdentitiesForSecondOwnIdentity() {
		return newHashSet(
				createIdentity("I21", "IN21", "IR21", asList("Test", "Test2"), ImmutableMap.of("KeyB", "ValueB"))
		);
	}

	private Set<Identity> createTrustedIdentitiesForThirdOwnIdentity() {
		return newHashSet(
				createIdentity("I31", "IN31", "IR31", asList("Test", "Test3"), ImmutableMap.of("KeyC", "ValueC"))
		);
	}

	private Set<Identity> createTrustedIdentitiesForFourthOwnIdentity() {
		return emptySet();
	}

	private OwnIdentity createOwnIdentity(String id, String nickname, String requestUri, String insertUri, List<String> contexts, ImmutableMap<String, String> properties) {
		OwnIdentity ownIdentity = new DefaultOwnIdentity(id, nickname, requestUri, insertUri);
		ownIdentity.setContexts(contexts);
		ownIdentity.setProperties(properties);
		return ownIdentity;
	}

	private Identity createIdentity(String id, String nickname, String requestUri, List<String> contexts, ImmutableMap<String, String> properties) {
		Identity identity = new DefaultIdentity(id, nickname, requestUri);
		identity.setContexts(contexts);
		identity.setProperties(properties);
		return identity;
	}

	@Test
	public void loadingIdentities() throws WebOfTrustException {
		List<OwnIdentity> ownIdentities = createOwnIdentities();
		Map<OwnIdentity, Collection<Identity>> identities = identityLoader.loadIdentities();
		verify(webOfTrustConnector).loadAllOwnIdentities();
		verify(webOfTrustConnector).loadTrustedIdentities(eq(ownIdentities.get(0)), eq(of("Test")));
		verify(webOfTrustConnector).loadTrustedIdentities(eq(ownIdentities.get(1)), eq(of("Test")));
		verify(webOfTrustConnector, never()).loadTrustedIdentities(eq(ownIdentities.get(2)), any(Optional.class));
		verify(webOfTrustConnector).loadTrustedIdentities(eq(ownIdentities.get(3)), eq(of("Test")));
		assertThat(identities.keySet(), hasSize(3));
		assertThat(identities.keySet(), containsInAnyOrder(ownIdentities.get(0), ownIdentities.get(1), ownIdentities.get(3)));
		verifyIdentitiesForOwnIdentity(identities, ownIdentities.get(0), createTrustedIdentitiesForFirstOwnIdentity());
		verifyIdentitiesForOwnIdentity(identities, ownIdentities.get(1), createTrustedIdentitiesForSecondOwnIdentity());
		verifyIdentitiesForOwnIdentity(identities, ownIdentities.get(3), createTrustedIdentitiesForFourthOwnIdentity());
	}

	@Test
	public void loadingIdentitiesWithoutContext() throws WebOfTrustException {
		List<OwnIdentity> ownIdentities = createOwnIdentities();
		Map<OwnIdentity, Collection<Identity>> identities = identityLoaderWithoutContext.loadIdentities();
		verify(webOfTrustConnector).loadAllOwnIdentities();
		verify(webOfTrustConnector).loadTrustedIdentities(eq(ownIdentities.get(0)), eq(Optional.<String>absent()));
		verify(webOfTrustConnector).loadTrustedIdentities(eq(ownIdentities.get(1)), eq(Optional.<String>absent()));
		verify(webOfTrustConnector).loadTrustedIdentities(eq(ownIdentities.get(2)), eq(Optional.<String>absent()));
		verify(webOfTrustConnector).loadTrustedIdentities(eq(ownIdentities.get(3)), eq(Optional.<String>absent()));
		assertThat(identities.keySet(), hasSize(4));
		OwnIdentity firstOwnIdentity = ownIdentities.get(0);
		OwnIdentity secondOwnIdentity = ownIdentities.get(1);
		OwnIdentity thirdOwnIdentity = ownIdentities.get(2);
		OwnIdentity fourthOwnIdentity = ownIdentities.get(3);
		assertThat(identities.keySet(), containsInAnyOrder(firstOwnIdentity, secondOwnIdentity, thirdOwnIdentity, fourthOwnIdentity));
		verifyIdentitiesForOwnIdentity(identities, firstOwnIdentity, createTrustedIdentitiesForFirstOwnIdentity());
		verifyIdentitiesForOwnIdentity(identities, secondOwnIdentity, createTrustedIdentitiesForSecondOwnIdentity());
		verifyIdentitiesForOwnIdentity(identities, thirdOwnIdentity, createTrustedIdentitiesForThirdOwnIdentity());
		verifyIdentitiesForOwnIdentity(identities, fourthOwnIdentity, createTrustedIdentitiesForFourthOwnIdentity());
	}

	private void verifyIdentitiesForOwnIdentity(Map<OwnIdentity, Collection<Identity>> identities, OwnIdentity ownIdentity, Set<Identity> trustedIdentities) {
		assertThat(identities.get(ownIdentity), Matchers.<Collection<Identity>>is(trustedIdentities));
	}

}
