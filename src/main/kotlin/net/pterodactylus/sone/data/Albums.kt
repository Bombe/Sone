/**
 * Sone - Albums.kt - Copyright © 2019–2020 David ‘Bombe’ Roden
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

/** Returns all images contained in this album and all its albums. */
val Album.allImages: Collection<Image>
	get() =
		images + albums.flatMap { it.allImages }

/**
 *  Returns this album and all albums contained in this album (recursively).
 * A child album is always listed after its parent.
 */
val Album.allAlbums: List<Album>
	get() =
		listOf(this) + albums.flatMap(Album::allAlbums)
