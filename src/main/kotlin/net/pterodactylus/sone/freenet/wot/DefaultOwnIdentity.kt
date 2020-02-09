/*
 * Sone - DefaultOwnIdentity.kt - Copyright © 2010–2019 David Roden
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
 * An own identity is an identity that the owner of the node has full control
 * over.
 */
class DefaultOwnIdentity(id: String, nickname: String, requestUri: String, private val insertUri: String) : DefaultIdentity(id, nickname, requestUri), OwnIdentity {

	override fun getInsertUri(): String {
		return insertUri
	}

	override fun addContext(context: String) = super.addContext(context) as OwnIdentity

	override fun removeContext(context: String) = super.removeContext(context) as OwnIdentity

	override fun setProperty(name: String, value: String) = super.setProperty(name, value) as OwnIdentity

	override fun removeProperty(name: String) = super.removeProperty(name) as OwnIdentity

}
