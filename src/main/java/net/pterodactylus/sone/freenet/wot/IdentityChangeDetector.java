/*
 * Sone - IdentityChangeDetector.java - Copyright © 2013–2019 David Roden
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
import java.util.Map.*;
import java.util.function.*;
import java.util.stream.*;

import com.google.common.collect.*;

/**
 * Detects changes between two lists of {@link Identity}s. The detector can find
 * added and removed identities, and for identities that exist in both list
 * their contexts and properties are checked for added, removed, or (in case of
 * properties) changed values.
 */
public class IdentityChangeDetector {

	private final Map<String, Identity> oldIdentities;
	private IdentityProcessor onNewIdentity;
	private IdentityProcessor onRemovedIdentity;
	private IdentityProcessor onChangedIdentity;
	private IdentityProcessor onUnchangedIdentity;

	public IdentityChangeDetector(Collection<? extends Identity> oldIdentities) {
		this.oldIdentities = convertToMap(oldIdentities);
	}

	public void onNewIdentity(IdentityProcessor onNewIdentity) {
		this.onNewIdentity = onNewIdentity;
	}

	public void onRemovedIdentity(IdentityProcessor onRemovedIdentity) {
		this.onRemovedIdentity = onRemovedIdentity;
	}

	public void onChangedIdentity(IdentityProcessor onChangedIdentity) {
		this.onChangedIdentity = onChangedIdentity;
	}

	public void onUnchangedIdentity(IdentityProcessor onUnchangedIdentity) {
		this.onUnchangedIdentity = onUnchangedIdentity;
	}

	public void detectChanges(final Collection<? extends Identity> newIdentities) {
		notifyForRemovedIdentities(oldIdentities.values().stream().filter(notContainedIn(newIdentities)).collect(Collectors.toList()));
		notifyForNewIdentities(newIdentities.stream().filter(notContainedIn(oldIdentities.values())).collect(Collectors.toList()));
		notifyForChangedIdentities(newIdentities.stream().filter(containedIn(oldIdentities)).filter(hasChanged(oldIdentities)).collect(Collectors.toList()));
		notifyForUnchangedIdentities(newIdentities.stream().filter(containedIn(oldIdentities)).filter(hasChanged(oldIdentities).negate()).collect(Collectors.toList()));
	}

	private void notifyForRemovedIdentities(Iterable<Identity> identities) {
		notify(onRemovedIdentity, identities);
	}

	private void notifyForNewIdentities(Iterable<? extends Identity> newIdentities) {
		notify(onNewIdentity, newIdentities);
	}

	private void notifyForChangedIdentities(Iterable<? extends Identity> identities) {
		notify(onChangedIdentity, identities);
	}

	private void notifyForUnchangedIdentities(Iterable<? extends Identity> identities) {
		notify(onUnchangedIdentity, identities);
	}

	private void notify(IdentityProcessor identityProcessor, Iterable<? extends Identity> identities) {
		if (identityProcessor == null) {
			return;
		}
		for (Identity identity : identities) {
			identityProcessor.processIdentity(identity);
		}
	}

	private static Predicate<Identity> hasChanged(final Map<String, Identity> oldIdentities) {
		return identity -> (identity != null) && identityHasChanged(oldIdentities.get(identity.getId()), identity);
	}

	private static boolean identityHasChanged(Identity oldIdentity, Identity newIdentity) {
		return identityHasNewContexts(oldIdentity, newIdentity)
				|| identityHasRemovedContexts(oldIdentity, newIdentity)
				|| identityHasNewProperties(oldIdentity, newIdentity)
				|| identityHasRemovedProperties(oldIdentity, newIdentity)
				|| identityHasChangedProperties(oldIdentity, newIdentity);
	}

	private static boolean identityHasNewContexts(Identity oldIdentity, Identity newIdentity) {
		return newIdentity.getContexts().stream().anyMatch(notAContextOf(oldIdentity));
	}

	private static boolean identityHasRemovedContexts(Identity oldIdentity, Identity newIdentity) {
		return oldIdentity.getContexts().stream().anyMatch(notAContextOf(newIdentity));
	}

	private static boolean identityHasNewProperties(Identity oldIdentity, Identity newIdentity) {
		return newIdentity.getProperties().entrySet().stream().anyMatch(notAPropertyOf(oldIdentity));
	}

	private static boolean identityHasRemovedProperties(Identity oldIdentity, Identity newIdentity) {
		return oldIdentity.getProperties().entrySet().stream().anyMatch(notAPropertyOf(newIdentity));
	}

	private static boolean identityHasChangedProperties(Identity oldIdentity, Identity newIdentity) {
		return oldIdentity.getProperties().entrySet().stream().anyMatch(hasADifferentValueThanIn(newIdentity));
	}

	private static Predicate<Identity> containedIn(final Map<String, Identity> identities) {
		return identity -> (identity != null) && identities.containsKey(identity.getId());
	}

	private static Predicate<String> notAContextOf(final Identity identity) {
		return context -> (identity != null) && !identity.getContexts().contains(context);
	}

	private static Predicate<Identity> notContainedIn(final Collection<? extends Identity> newIdentities) {
		return identity -> (identity != null) && !newIdentities.contains(identity);
	}

	private static Predicate<Entry<String, String>> notAPropertyOf(final Identity identity) {
		return property -> (property != null) && !identity.getProperties().containsKey(property.getKey());
	}

	private static Predicate<Entry<String, String>> hasADifferentValueThanIn(final Identity newIdentity) {
		return property -> (property != null) && !newIdentity.getProperty(property.getKey()).equals(property.getValue());
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
