/*
 * Sone - Image.java - Copyright © 2011 David Roden
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

import java.util.UUID;

/**
 * Container for image metadata.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Image {

	/** The ID of the image. */
	private final String id;

	/** The key of the image. */
	private final String key;

	/** The creation time of the image. */
	private final long creationTime;

	/** The width of the image. */
	private final int width;

	/** The height of the image. */
	private final int height;

	/** The title of the image. */
	private String title;

	/** The description of the image. */
	private String description;

	/**
	 * Creates a new image.
	 *
	 * @param key
	 *            The key of the image
	 * @param creationTime
	 *            The creation time of the image
	 * @param width
	 *            The width of the image
	 * @param height
	 *            The height of the image
	 */
	public Image(String key, long creationTime, int width, int height) {
		this(UUID.randomUUID().toString(), key, creationTime, width, height);
	}

	/**
	 * Creates a new image.
	 *
	 * @param id
	 *            The ID of the image
	 * @param key
	 *            The key of the image
	 * @param creationTime
	 *            The creation time of the image
	 * @param width
	 *            The width of the image
	 * @param height
	 *            The height of the image
	 */
	public Image(String id, String key, long creationTime, int width, int height) {
		this.id = id;
		this.key = key;
		this.creationTime = creationTime;
		this.width = width;
		this.height = height;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the ID of this image.
	 *
	 * @return The ID of this image
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the key of this image.
	 *
	 * @return The key of this image
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Returns the creation time of this image.
	 *
	 * @return The creation time of this image (in milliseconds since 1970, Jan
	 *         1, UTC)
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * Returns the width of this image.
	 *
	 * @return The width of this image (in pixels)
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Returns the height of this image.
	 *
	 * @return The height of this image (in pixels)
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns the title of this image.
	 *
	 * @return The title of this image
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title of this image.
	 *
	 * @param title
	 *            The title of this image
	 * @return This image
	 */
	public Image setTitle(String title) {
		this.title = title;
		return this;
	}

	/**
	 * Returns the description of this image.
	 *
	 * @return The description of this image
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of this image.
	 *
	 * @param description
	 *            The description of this image
	 * @return This image
	 */
	public Image setDescription(String description) {
		this.description = description;
		return this;
	}

}
