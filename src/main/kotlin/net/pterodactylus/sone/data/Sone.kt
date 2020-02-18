/**
 * Sone - Sone.kt - Copyright © 2020 David ‘Bombe’ Roden
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

import net.pterodactylus.sone.template.*
import java.util.Comparator.*

private val caseInsensitiveCompare = { left: String, right: String -> left.compareTo(right, true) }

/**
 * Comparator that sorts Sones by their [nice name][SoneAccessor.getNiceName]
 * and, failing that, by [ID][Sone.id].
 */
@get:JvmName("niceNameComparator") // TODO: remove once Sone is 100% Kotlin
val niceNameComparator: Comparator<Sone> =
		comparing(SoneAccessor::getNiceName, caseInsensitiveCompare).thenComparing(Sone::id)

/**
 * Comparator that sorts Sones by their [last activity][Sone.getTime], least
 * recently active Sones first.
 */
@get:JvmName("lastActivityComparator") // TODO: remove once Sone is 100% Kotlin
val lastActivityComparator: Comparator<Sone> =
		comparing(Sone::getTime).reversed()

/**
 * Comparator that sorts Sones by their [post count][Sone.getPosts] (most posts
 * first) and, failing that, by their [reply count][Sone.getReplies] (most
 * replies first).
 */
@get:JvmName("postCountComparator") // TODO: remove once Sone is 100% Kotlin
val postCountComparator: Comparator<Sone> =
		comparing<Sone, Int> { it.posts.size }
				.thenComparing<Int> { it.replies.size }
				.reversed()

val imageCountComparator: Comparator<Sone> =
		comparing<Sone, Int> { it.rootAlbum.allImages.size }.reversed()

val Sone.allAlbums: List<Album>
	get() =
		rootAlbum.albums.flatMap(Album::allAlbums)

val Sone.allImages: Collection<Image>
	get() =
		rootAlbum.allImages
