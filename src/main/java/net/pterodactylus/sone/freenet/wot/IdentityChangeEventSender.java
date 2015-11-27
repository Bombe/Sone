/*
 * Sone - IdentityChangeEventSender.java - Copyright © 2013–2015 David Roden
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

import java.util.Collection;
import java.util.Map;

import net.pterodactylus.sone.freenet.wot.IdentityChangeDetector.IdentityProcessor;
import net.pterodactylus.sone.freenet.wot.event.IdentityAddedEvent;
import net.pterodactylus.sone.freenet.wot.event.IdentityRemovedEvent;
import net.pterodactylus.sone.freenet.wot.event.IdentityUpdatedEvent;
import net.pterodactylus.sone.freenet.wot.event.OwnIdentityAddedEvent;
import net.pterodactylus.sone.freenet.wot.event.OwnIdentityRemovedEvent;

import com.google.common.eventbus.EventBus;

/**
 * Detects changes in {@link Identity}s trusted my multiple {@link
 * OwnIdentity}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 * @see IdentityChangeDetector
 */
public class IdentityChangeEventSender {

	private final EventBus eventBus;
	private final Map<OwnIdentity, Collection<Identity>> oldIdentities;

	public IdentityChangeEventSender(EventBus eventBus, Map<OwnIdentity, Collection<Identity>> oldIdentities) {
		this.eventBus = eventBus;
		this.oldIdentities = oldIdentities;
	}

	public void detectChanges(Map<OwnIdentity, Collection<Identity>> identities) {
		IdentityChangeDetector identityChangeDetector = new IdentityChangeDetector(oldIdentities.keySet());
		identityChangeDetector.onNewIdentity(addNewOwnIdentityAndItsTrustedIdentities(identities));
		identityChangeDetector.onRemovedIdentity(removeOwnIdentityAndItsTrustedIdentities(oldIdentities));
		identityChangeDetector.onUnchangedIdentity(detectChangesInTrustedIdentities(identities, oldIdentities));
		identityChangeDetector.detectChanges(identities.keySet());
	}

	private IdentityProcessor addNewOwnIdentityAndItsTrustedIdentities(final Map<OwnIdentity, Collection<Identity>> newIdentities) {
		return new IdentityProcessor() {
			@Override
			public void processIdentity(Identity identity) {
				eventBus.post(new OwnIdentityAddedEvent((OwnIdentity) identity));
				for (Identity newIdentity : newIdentities.get((OwnIdentity) identity)) {
					eventBus.post(new IdentityAddedEvent((OwnIdentity) identity, newIdentity));
				}
			}
		};
	}

	private IdentityProcessor removeOwnIdentityAndItsTrustedIdentities(final Map<OwnIdentity, Collection<Identity>> oldIdentities) {
		return new IdentityProcessor() {
			@Override
			public void processIdentity(Identity identity) {
				eventBus.post(new OwnIdentityRemovedEvent((OwnIdentity) identity));
				for (Identity removedIdentity : oldIdentities.get((OwnIdentity) identity)) {
					eventBus.post(new IdentityRemovedEvent((OwnIdentity) identity, removedIdentity));
				}
			}
		};
	}

	private IdentityProcessor detectChangesInTrustedIdentities(Map<OwnIdentity, Collection<Identity>> newIdentities, Map<OwnIdentity, Collection<Identity>> oldIdentities) {
		return new DefaultIdentityProcessor(oldIdentities, newIdentities);
	}

	private class DefaultIdentityProcessor implements IdentityProcessor {

		private final Map<OwnIdentity, Collection<Identity>> oldIdentities;
		private final Map<OwnIdentity, Collection<Identity>> newIdentities;

		public DefaultIdentityProcessor(Map<OwnIdentity, Collection<Identity>> oldIdentities, Map<OwnIdentity, Collection<Identity>> newIdentities) {
			this.oldIdentities = oldIdentities;
			this.newIdentities = newIdentities;
		}

		@Override
		public void processIdentity(Identity ownIdentity) {
			IdentityChangeDetector identityChangeDetector = new IdentityChangeDetector(oldIdentities.get((OwnIdentity) ownIdentity));
			identityChangeDetector.onNewIdentity(notifyForAddedIdentities((OwnIdentity) ownIdentity));
			identityChangeDetector.onRemovedIdentity(notifyForRemovedIdentities((OwnIdentity) ownIdentity));
			identityChangeDetector.onChangedIdentity(notifyForChangedIdentities((OwnIdentity) ownIdentity));
			identityChangeDetector.detectChanges(newIdentities.get((OwnIdentity) ownIdentity));
		}

		private IdentityProcessor notifyForChangedIdentities(final OwnIdentity ownIdentity) {
			return new IdentityProcessor() {
				@Override
				public void processIdentity(Identity identity) {
					eventBus.post(new IdentityUpdatedEvent(ownIdentity, identity));
				}
			};
		}

		private IdentityProcessor notifyForRemovedIdentities(final OwnIdentity ownIdentity) {
			return new IdentityProcessor() {
				@Override
				public void processIdentity(Identity identity) {
					eventBus.post(new IdentityRemovedEvent(ownIdentity, identity));
				}
			};
		}

		private IdentityProcessor notifyForAddedIdentities(final OwnIdentity ownIdentity) {
			return new IdentityProcessor() {
				@Override
				public void processIdentity(Identity identity) {
					eventBus.post(new IdentityAddedEvent(ownIdentity, identity));
				}
			};
		}

	}

}
