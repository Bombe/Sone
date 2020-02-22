/**
 * Sone - Reply.kt - Copyright © 2020 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.data

import java.util.Comparator.comparing

/**
 * Comparator that orders replies by their time, newest replies first.
 */
@get:JvmName("newestReplyFirst")
val newestReplyFirst: Comparator<Reply<*>> =
		comparing(Reply<*>::getTime).reversed()

/**
 * Predicate that returns whether a reply is _not_ from the future,
 * i.e. whether it should be visible now.
 */
val noFutureReply: (Reply<*>) -> Boolean =
		{ it.getTime() <= System.currentTimeMillis() }
