/*
 * Sone - ReplyBuilder.java - Copyright © 2013–2019 David Roden
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

package net.pterodactylus.sone.database

import net.pterodactylus.sone.data.*

/**
 * Methods that all reply builders need to implement in order to be able to
 * create any kind of [Reply].
 *
 * @param B The type of the builder
 */
interface ReplyBuilder<B : ReplyBuilder<B>> {

	fun randomId(): B
	fun withId(id: String): B

	fun from(senderId: String): B
	fun currentTime(): B
	fun withTime(time: Long): B
	fun withText(text: String): B

}
