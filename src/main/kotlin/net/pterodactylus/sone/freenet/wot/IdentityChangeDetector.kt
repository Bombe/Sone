/*
 * Sone - IdentityChangeDetector.kt - Copyright © 2013–2020 David Roden
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

package net.pterodactylus.sone.freenet.wot

/**
 * Detects changes between two lists of [Identity]s. The detector can find
 * added and removed identities, and for identities that exist in both list
 * their contexts and properties are checked for added, removed, or (in case of
 * properties) changed values.
 */
class IdentityChangeDetector(oldIdentities: Collection<Identity>) {

	private val oldIdentities: Map<String, Identity> = oldIdentities.associateBy { it.id }
	var onNewIdentity: IdentityProcessor? = null
	var onRemovedIdentity: IdentityProcessor? = null
	var onChangedIdentity: IdentityProcessor? = null
	var onUnchangedIdentity: IdentityProcessor? = null

	fun detectChanges(newIdentities: Collection<Identity>) {
		onRemovedIdentity.notify(oldIdentities.values.filter { it !in newIdentities })
		onNewIdentity.notify(newIdentities.filter { it !in oldIdentities.values })
		onChangedIdentity.notify(newIdentities.filter { it.id in oldIdentities }.filter { identityHasChanged(oldIdentities[it.id]!!, it) })
		onUnchangedIdentity.notify(newIdentities.filter { it.id in oldIdentities }.filterNot { identityHasChanged(oldIdentities[it.id]!!, it) })
	}

	private fun identityHasChanged(oldIdentity: Identity, newIdentity: Identity?) =
			identityHasNewContexts(oldIdentity, newIdentity!!)
					|| identityHasRemovedContexts(oldIdentity, newIdentity)
					|| identityHasNewProperties(oldIdentity, newIdentity)
					|| identityHasRemovedProperties(oldIdentity, newIdentity)
					|| identityHasChangedProperties(oldIdentity, newIdentity)

	private fun identityHasNewContexts(oldIdentity: Identity, newIdentity: Identity) =
			newIdentity.contexts.any { it !in oldIdentity.contexts }

	private fun identityHasRemovedContexts(oldIdentity: Identity, newIdentity: Identity) =
			oldIdentity.contexts.any { it !in newIdentity.contexts }

	private fun identityHasNewProperties(oldIdentity: Identity, newIdentity: Identity) =
			newIdentity.properties.keys.any { it !in oldIdentity.properties }

	private fun identityHasRemovedProperties(oldIdentity: Identity, newIdentity: Identity) =
			oldIdentity.properties.keys.any { it !in newIdentity.properties }

	private fun identityHasChangedProperties(oldIdentity: Identity, newIdentity: Identity) =
			oldIdentity.properties.entries.any { newIdentity.properties[it.key] != it.value }

}

typealias IdentityProcessor = (Identity) -> Unit

private fun IdentityProcessor?.notify(identities: Iterable<Identity>) =
		this?.let { identities.forEach(this::invoke) }
