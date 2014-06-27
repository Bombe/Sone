/*
 * Sone - IdentityChangeEventSenderTest.java - Copyright © 2013 David Roden
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
import static java.util.Arrays.asList;
import static net.pterodactylus.sone.freenet.wot.Identities.createIdentity;
import static net.pterodactylus.sone.freenet.wot.Identities.createOwnIdentity;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import net.pterodactylus.sone.freenet.wot.event.IdentityAddedEvent;
import net.pterodactylus.sone.freenet.wot.event.IdentityRemovedEvent;
import net.pterodactylus.sone.freenet.wot.event.IdentityUpdatedEvent;
import net.pterodactylus.sone.freenet.wot.event.OwnIdentityAddedEvent;
import net.pterodactylus.sone.freenet.wot.event.OwnIdentityRemovedEvent;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import org.junit.Test;

/**
 * Unit test for {@link IdentityChangeEventSender}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IdentityChangeEventSenderTest {

	private final EventBus eventBus = mock(EventBus.class);
	private final List<OwnIdentity> ownIdentities = asList(
			createOwnIdentity("O1", asList("Test"), of("KeyA", "ValueA")),
			createOwnIdentity("O2", asList("Test2"), of("KeyB", "ValueB")),
			createOwnIdentity("O3", asList("Test3"), of("KeyC", "ValueC"))
	);
	private final List<Identity> identities = asList(
			createIdentity("I1", Collections.<String>emptyList(), Collections.<String, String>emptyMap()),
			createIdentity("I2", Collections.<String>emptyList(), Collections.<String, String>emptyMap()),
			createIdentity("I3", Collections.<String>emptyList(), Collections.<String, String>emptyMap()),
			createIdentity("I2", asList("Test"), Collections.<String, String>emptyMap())
	);
	private final IdentityChangeEventSender identityChangeEventSender = new IdentityChangeEventSender(eventBus, createOldIdentities());

	@Test
	public void addingAnOwnIdentityIsDetectedAndReportedCorrectly() {
		Multimap<OwnIdentity, Identity> newIdentities = createNewIdentities();
		identityChangeEventSender.detectChanges(newIdentities);
		verify(eventBus).post(eq(new OwnIdentityRemovedEvent(ownIdentities.get(0))));
		verify(eventBus).post(eq(new IdentityRemovedEvent(ownIdentities.get(0), identities.get(0))));
		verify(eventBus).post(eq(new IdentityRemovedEvent(ownIdentities.get(0), identities.get(1))));
		verify(eventBus).post(eq(new OwnIdentityAddedEvent(ownIdentities.get(2))));
		verify(eventBus).post(eq(new IdentityAddedEvent(ownIdentities.get(2), identities.get(1))));
		verify(eventBus).post(eq(new IdentityAddedEvent(ownIdentities.get(2), identities.get(2))));
		verify(eventBus).post(eq(new IdentityRemovedEvent(ownIdentities.get(1), identities.get(0))));
		verify(eventBus).post(eq(new IdentityAddedEvent(ownIdentities.get(1), identities.get(2))));
		verify(eventBus).post(eq(new IdentityUpdatedEvent(ownIdentities.get(1), identities.get(1))));
	}

	private Multimap<OwnIdentity, Identity> createNewIdentities() {
		ImmutableMultimap.Builder<OwnIdentity, Identity> oldIdentities = ImmutableMultimap.builder();
		oldIdentities.put(ownIdentities.get(1), identities.get(3));
		oldIdentities.put(ownIdentities.get(1), identities.get(2));
		oldIdentities.put(ownIdentities.get(2), identities.get(1));
		oldIdentities.put(ownIdentities.get(2), identities.get(2));
		return oldIdentities.build();
	}

	private Multimap<OwnIdentity, Identity> createOldIdentities() {
		ImmutableMultimap.Builder<OwnIdentity, Identity> oldIdentities = ImmutableMultimap.builder();
		oldIdentities.put(ownIdentities.get(0), identities.get(0));
		oldIdentities.put(ownIdentities.get(0), identities.get(1));
		oldIdentities.put(ownIdentities.get(1), identities.get(0));
		oldIdentities.put(ownIdentities.get(1), identities.get(1));
		return oldIdentities.build();
	}

}
