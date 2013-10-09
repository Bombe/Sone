/*
 * Sone - AlbumBuilder.java - Copyright © 2013 David Roden
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

/**
 * Builder for {@link Album} objects.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface AlbumBuilder {

	/**
	 * Configures this builder to create an album with a random ID.
	 *
	 * @return This album builder
	 */
	AlbumBuilder randomId();

	/**
	 * Configures this builder to create an album with the given ID.
	 *
	 * @param id
	 * 		The ID of the album
	 * @return This album builder
	 */
	AlbumBuilder withId(String id);

	/**
	 * Creates the album.
	 *
	 * @return The created album
	 * @throws IllegalStateException
	 * 		if the album could not be created
	 */
	Album build() throws IllegalStateException;

}
