/*
 * Sone - AlbumProvider.java - Copyright © 2013–2015 David Roden
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

package net.pterodactylus.sone.database;

import net.pterodactylus.sone.data.Album;

import com.google.common.base.Optional;

/**
 * Interface for objects that can provide {@link Album}s by their ID.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface AlbumProvider {

	/**
	 * Returns the album with the given ID, or {@link Optional#absent()} if no such
	 * album exists.
	 *
	 * @param albumId
	 * 		The ID of the album
	 * @return The album, or {@link Optional#absent()} if the album does not exist
	 */
	Optional<Album> getAlbum(String albumId);

}
