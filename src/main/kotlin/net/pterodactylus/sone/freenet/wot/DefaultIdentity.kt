/*
 * Sone - DefaultIdentity.kt - Copyright © 2010–2020 David Roden
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

import java.util.Collections.*

/**
 * A Web of Trust identity.
 */
open class DefaultIdentity(private val id: String, private val nickname: String?, private val requestUri: String) : Identity {

	private val contexts = mutableSetOf<String>().synchronized()
	private val properties = mutableMapOf<String, String>().synchronized()
	private val trustCache = mutableMapOf<OwnIdentity, Trust>().synchronized()

	override fun getId() = id
	override fun getNickname() = nickname
	override fun getRequestUri() = requestUri
	override fun getContexts() = synchronized(contexts) { contexts.toSet() }

	override fun hasContext(context: String) = context in contexts

	override fun setContexts(contexts: Set<String>) {
		synchronized(this.contexts) {
			this.contexts.clear()
			this.contexts.addAll(contexts)
		}
	}

	override fun addContext(context: String): Identity = apply {
		synchronized(this.contexts) {
			contexts += context
		}
	}

	override fun removeContext(context: String): Identity = apply {
		synchronized(this.contexts) {
			contexts -= context
		}
	}

	override fun getProperties() = synchronized(properties) { properties.toMap() }

	override fun setProperties(properties: Map<String, String>) {
		synchronized(this.properties) {
			this.properties.clear()
			this.properties.putAll(properties)
		}
	}

	override fun getProperty(name: String) = synchronized(properties) { properties[name] }

	override fun setProperty(name: String, value: String): Identity = apply {
		synchronized(properties) {
			properties[name] = value
		}
	}

	override fun removeProperty(name: String): Identity = apply {
		synchronized(properties) {
			properties -= name
		}
	}

	override fun getTrust(ownIdentity: OwnIdentity) = synchronized(trustCache) {
		trustCache[ownIdentity]
	}

	override fun setTrust(ownIdentity: OwnIdentity, trust: Trust) = apply {
		synchronized(trustCache) {
			trustCache[ownIdentity] = trust
		}
	}

	override fun removeTrust(ownIdentity: OwnIdentity) = apply {
		synchronized(trustCache) {
			trustCache -= ownIdentity
		}
	}

	override fun hashCode() = id.hashCode()

	override fun equals(other: Any?) = if (other !is Identity) {
		false
	} else {
		other.id == getId()
	}

	override fun toString() = "${javaClass.simpleName}[id=$id,nickname=$nickname,contexts=$contexts,properties=$properties]"

}

private fun <T> Set<T>.synchronized(): MutableSet<T> = synchronizedSet(this)
private fun <K, V> Map<K, V>.synchronized(): MutableMap<K, V> = synchronizedMap(this)
