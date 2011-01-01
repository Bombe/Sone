/*
 * Sone - Album.java - Copyright © 2011 David Roden
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

package net.pterodactylus.sone.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for images that can also contain nested {@link Album}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Album {

	/** Nested albums. */
	private final List<Album> albums = new ArrayList<Album>();

	/** The images in this album. */
	private final List<Image> images = new ArrayList<Image>();

	/** The name of this album. */
	private String name;

	/** The description of this album. */
	private String description;

	//
	// ACCESSORS
	//

	/**
	 * Returns the nested albums.
	 *
	 * @return The nested albums
	 */
	public List<Album> getNestedAlbums() {
		return new ArrayList<Album>(albums);
	}

	/**
	 * Returns the images in this album.
	 *
	 * @return The images in this album
	 */
	public List<Image> getImages() {
		return new ArrayList<Image>(images);
	}

	/**
	 * Returns the name of this album.
	 *
	 * @return The name of this album
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this album.
	 *
	 * @param name
	 *            The name of this album
	 * @return This album
	 */
	public Album setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Returns the description of this album.
	 *
	 * @return The description of this album
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of this album.
	 *
	 * @param description
	 *            The description of this album
	 * @return This album
	 */
	public Album setDescription(String description) {
		this.description = description;
		return this;
	}

}
