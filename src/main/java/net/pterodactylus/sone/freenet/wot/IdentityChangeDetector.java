/*
 * Sone - IdentityChangeDetector.java - Copyright © 2013 David Roden
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

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.FluentIterable.from;
import static net.pterodactylus.sone.freenet.wot.Identity.TO_CONTEXTS;
import static net.pterodactylus.sone.freenet.wot.Identity.TO_PROPERTIES;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;

/**
 * Detects changes between two lists of {@link Identity}s. The detector can find
 * added and removed identities, and for identities that exist in both list
 * their contexts and properties are checked for added, removed, or (in case of
 * properties) changed values.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IdentityChangeDetector {

	private final Map<String, Identity> oldIdentities;
	private Optional<IdentityProcessor> onNewIdentity = absent();
	private Optional<IdentityProcessor> onRemovedIdentity = absent();
	private Optional<IdentityProcessor> onChangedIdentity = absent();
	private Optional<IdentityProcessor> onUnchangedIdentity = absent();

	public IdentityChangeDetector(Collection<? extends Identity> oldIdentities) {
		this.oldIdentities = convertToMap(oldIdentities);
	}

	public void onNewIdentity(IdentityProcessor onNewIdentity) {
		this.onNewIdentity = fromNullable(onNewIdentity);
	}

	public void onRemovedIdentity(IdentityProcessor onRemovedIdentity) {
		this.onRemovedIdentity = fromNullable(onRemovedIdentity);
	}

	public void onChangedIdentity(IdentityProcessor onChangedIdentity) {
		this.onChangedIdentity = fromNullable(onChangedIdentity);
	}

	public void onUnchangedIdentity(IdentityProcessor onUnchangedIdentity) {
		this.onUnchangedIdentity = fromNullable(onUnchangedIdentity);
	}

	public void detectChanges(final Collection<? extends Identity> newIdentities) {
		notifyForRemovedIdentities(from(oldIdentities.values()).filter(notContainedIn(newIdentities)));
		notifyForNewIdentities(from(newIdentities).filter(notContainedIn(oldIdentities.values())));
		notifyForChangedIdentities(from(newIdentities).filter(containedIn(oldIdentities)).filter(hasChanged(oldIdentities)));
		notifyForUnchangedIdentities(from(newIdentities).filter(containedIn(oldIdentities)).filter(not(hasChanged(oldIdentities))));
	}

	private void notifyForRemovedIdentities(Iterable<Identity> identities) {
		notify(onRemovedIdentity, identities);
	}

	private void notifyForNewIdentities(FluentIterable<? extends Identity> newIdentities) {
		notify(onNewIdentity, newIdentities);
	}

	private void notifyForChangedIdentities(FluentIterable<? extends Identity> identities) {
		notify(onChangedIdentity, identities);
	}

	private void notifyForUnchangedIdentities(FluentIterable<? extends Identity> identities) {
		notify(onUnchangedIdentity, identities);
	}

	private void notify(Optional<IdentityProcessor> identityProcessor, Iterable<? extends Identity> identities) {
		if (!identityProcessor.isPresent()) {
			return;
		}
		for (Identity identity : identities) {
			identityProcessor.get().processIdentity(identity);
		}
	}

	private static Predicate<Identity> hasChanged(final Map<String, Identity> oldIdentities) {
		return new Predicate<Identity>() {
			@Override
			public boolean apply(Identity identity) {
				return (identity == null) ? false : identityHasChanged(oldIdentities.get(identity.getId()), identity);
			}
		};
	}

	private static boolean identityHasChanged(Identity oldIdentity, Identity newIdentity) {
		return identityHasNewContexts(oldIdentity, newIdentity)
				|| identityHasRemovedContexts(oldIdentity, newIdentity)
				|| identityHasNewProperties(oldIdentity, newIdentity)
				|| identityHasRemovedProperties(oldIdentity, newIdentity)
				|| identityHasChangedProperties(oldIdentity, newIdentity);
	}

	private static boolean identityHasNewContexts(Identity oldIdentity, Identity newIdentity) {
		return from(TO_CONTEXTS.apply(newIdentity)).anyMatch(notAContextOf(oldIdentity));
	}

	private static boolean identityHasRemovedContexts(Identity oldIdentity, Identity newIdentity) {
		return from(TO_CONTEXTS.apply(oldIdentity)).anyMatch(notAContextOf(newIdentity));
	}

	private static boolean identityHasNewProperties(Identity oldIdentity, Identity newIdentity) {
		return from(TO_PROPERTIES.apply(newIdentity).entrySet()).anyMatch(notAPropertyOf(oldIdentity));
	}

	private static boolean identityHasRemovedProperties(Identity oldIdentity, Identity newIdentity) {
		return from(TO_PROPERTIES.apply(oldIdentity).entrySet()).anyMatch(notAPropertyOf(newIdentity));
	}

	private static boolean identityHasChangedProperties(Identity oldIdentity, Identity newIdentity) {
		return from(TO_PROPERTIES.apply(oldIdentity).entrySet()).anyMatch(hasADifferentValueThanIn(newIdentity));
	}

	private static Predicate<Identity> containedIn(final Map<String, Identity> identities) {
		return new Predicate<Identity>() {
			@Override
			public boolean apply(Identity identity) {
				return identities.containsKey(identity.getId());
			}
		};
	}

	private static Predicate<String> notAContextOf(final Identity identity) {
		return new Predicate<String>() {
			@Override
			public boolean apply(String context) {
				return (identity == null) ? false : !identity.getContexts().contains(context);
			}
		};
	}

	private static Predicate<Identity> notContainedIn(final Collection<? extends Identity> newIdentities) {
		return new Predicate<Identity>() {
			@Override
			public boolean apply(Identity identity) {
				return (identity == null) ? false : !newIdentities.contains(identity);
			}
		};
	}

	private static Predicate<Entry<String, String>> notAPropertyOf(final Identity identity) {
		return new Predicate<Entry<String, String>>() {
			@Override
			public boolean apply(Entry<String, String> property) {
				return (property == null) ? false : !identity.getProperties().containsKey(property.getKey());
			}
		};
	}

	private static Predicate<Entry<String, String>> hasADifferentValueThanIn(final Identity newIdentity) {
		return new Predicate<Entry<String, String>>() {
			@Override
			public boolean apply(Entry<String, String> property) {
				return (property == null) ? false : !newIdentity.getProperty(property.getKey()).equals(property.getValue());
			}
		};
	}

	private static Map<String, Identity> convertToMap(Collection<? extends Identity> identities) {
		ImmutableMap.Builder<String, Identity> mapBuilder = ImmutableMap.builder();
		for (Identity identity : identities) {
			mapBuilder.put(identity.getId(), identity);
		}
		return mapBuilder.build();
	}

	public interface IdentityProcessor {

		void processIdentity(Identity identity);

	}

}
