/*
 * Sone - IdentityChangeDetectorTest.java - Copyright © 2013–2019 David Roden
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

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static net.pterodactylus.sone.freenet.wot.Identities.createIdentity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

import java.util.Collection;

import net.pterodactylus.sone.freenet.wot.IdentityChangeDetector.IdentityProcessor;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link IdentityChangeDetector}.
 */
public class IdentityChangeDetectorTest {

	private final IdentityChangeDetector identityChangeDetector = new IdentityChangeDetector(createOldIdentities());
	private final Collection<Identity> newIdentities = newArrayList();
	private final Collection<Identity> removedIdentities = newArrayList();
	private final Collection<Identity> changedIdentities = newArrayList();
	private final Collection<Identity> unchangedIdentities = newArrayList();

	@Before
	public void setup() {
		identityChangeDetector.onNewIdentity(new IdentityProcessor() {
			@Override
			public void processIdentity(Identity identity) {
				newIdentities.add(identity);
			}
		});
		identityChangeDetector.onRemovedIdentity(new IdentityProcessor() {
			@Override
			public void processIdentity(Identity identity) {
				removedIdentities.add(identity);
			}
		});
		identityChangeDetector.onChangedIdentity(new IdentityProcessor() {
			@Override
			public void processIdentity(Identity identity) {
				changedIdentities.add(identity);
			}
		});
		identityChangeDetector.onUnchangedIdentity(new IdentityProcessor() {
			@Override
			public void processIdentity(Identity identity) {
				unchangedIdentities.add(identity);
			}
		});
	}

	@Test
	public void noDifferencesAreDetectedWhenSendingTheOldIdentitiesAgain() {
		identityChangeDetector.detectChanges(createOldIdentities());
		assertThat(newIdentities, empty());
		assertThat(removedIdentities, empty());
		assertThat(changedIdentities, empty());
		assertThat(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity2(), createIdentity3()));
	}

	@Test
	public void detectThatAnIdentityWasRemoved() {
		identityChangeDetector.detectChanges(asList(createIdentity1(), createIdentity3()));
		assertThat(newIdentities, empty());
		assertThat(removedIdentities, containsInAnyOrder(createIdentity2()));
		assertThat(changedIdentities, empty());
		assertThat(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity3()));
	}

	@Test
	public void detectThatAnIdentityWasAdded() {
		identityChangeDetector.detectChanges(asList(createIdentity1(), createIdentity2(), createIdentity3(), createIdentity4()));
		assertThat(newIdentities, containsInAnyOrder(createIdentity4()));
		assertThat(removedIdentities, empty());
		assertThat(changedIdentities, empty());
		assertThat(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity2(), createIdentity3()));
	}

	@Test
	public void detectThatAContextWasRemoved() {
		Identity identity2 = createIdentity2();
		identity2.removeContext("Context C");
		identityChangeDetector.detectChanges(asList(createIdentity1(), identity2, createIdentity3()));
		assertThat(newIdentities, empty());
		assertThat(removedIdentities, empty());
		assertThat(changedIdentities, containsInAnyOrder(identity2));
		assertThat(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity3()));
	}

	@Test
	public void detectThatAContextWasAdded() {
		Identity identity2 = createIdentity2();
		identity2.addContext("Context C1");
		identityChangeDetector.detectChanges(asList(createIdentity1(), identity2, createIdentity3()));
		assertThat(newIdentities, empty());
		assertThat(removedIdentities, empty());
		assertThat(changedIdentities, containsInAnyOrder(identity2));
		assertThat(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity3()));
	}

	@Test
	public void detectThatAPropertyWasRemoved() {
		Identity identity1 = createIdentity1();
		identity1.removeProperty("Key A");
		identityChangeDetector.detectChanges(asList(identity1, createIdentity2(), createIdentity3()));
		assertThat(newIdentities, empty());
		assertThat(removedIdentities, empty());
		assertThat(changedIdentities, containsInAnyOrder(identity1));
		assertThat(unchangedIdentities, containsInAnyOrder(createIdentity2(), createIdentity3()));
	}

	@Test
	public void detectThatAPropertyWasAdded() {
		Identity identity3 = createIdentity3();
		identity3.setProperty("Key A", "Value A");
		identityChangeDetector.detectChanges(asList(createIdentity1(), createIdentity2(), identity3));
		assertThat(newIdentities, empty());
		assertThat(removedIdentities, empty());
		assertThat(changedIdentities, containsInAnyOrder(identity3));
		assertThat(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity2()));
	}

	@Test
	public void detectThatAPropertyWasChanged() {
		Identity identity3 = createIdentity3();
		identity3.setProperty("Key E", "Value F");
		identityChangeDetector.detectChanges(asList(createIdentity1(), createIdentity2(), identity3));
		assertThat(newIdentities, empty());
		assertThat(removedIdentities, empty());
		assertThat(changedIdentities, containsInAnyOrder(identity3));
		assertThat(unchangedIdentities, containsInAnyOrder(createIdentity1(), createIdentity2()));
	}

	@Test
	public void noRemovedIdentitiesAreDetectedWithoutAnIdentityProcessor() {
		identityChangeDetector.onRemovedIdentity(null);
		identityChangeDetector.detectChanges(asList(createIdentity1(), createIdentity3()));
	}

	@Test
	public void noAddedIdentitiesAreDetectedWithoutAnIdentityProcessor() {
		identityChangeDetector.onNewIdentity(null);
		identityChangeDetector.detectChanges(asList(createIdentity1(), createIdentity2(), createIdentity3(), createIdentity4()));
	}

	private static Collection<Identity> createOldIdentities() {
		return asList(createIdentity1(), createIdentity2(), createIdentity3());
	}

	private static Identity createIdentity1() {
		return createIdentity("Test1", asList("Context A", "Context B"), of("Key A", "Value A", "Key B", "Value B"));
	}

	private static Identity createIdentity2() {
		return createIdentity("Test2", asList("Context C", "Context D"), of("Key C", "Value C", "Key D", "Value D"));
	}

	private static Identity createIdentity3() {
		return createIdentity("Test3", asList("Context E", "Context F"), of("Key E", "Value E", "Key F", "Value F"));
	}

	private static Identity createIdentity4() {
		return createIdentity("Test4", asList("Context G", "Context H"), of("Key G", "Value G", "Key H", "Value H"));
	}

}
