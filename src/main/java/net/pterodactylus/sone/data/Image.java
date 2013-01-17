/*
 * Sone - Image.java - Copyright © 2011–2012 David Roden
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.UUID;

/**
 * Container for image metadata.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Image implements Fingerprintable {

	/** The ID of the image. */
	private final String id;

	/** The Sone the image belongs to. */
	private Sone sone;

	/** The album this image belongs to. */
	private Album album;

	/** The request key of the image. */
	private String key;

	/** The creation time of the image. */
	private long creationTime;

	/** The width of the image. */
	private int width;

	/** The height of the image. */
	private int height;

	/** The title of the image. */
	private String title;

	/** The description of the image. */
	private String description;

	/**
	 * Creates a new image with a random ID.
	 */
	public Image() {
		this(UUID.randomUUID().toString());
		setCreationTime(System.currentTimeMillis());
	}

	/**
	 * Creates a new image.
	 *
	 * @param id
	 *            The ID of the image
	 */
	public Image(String id) {
		this.id = checkNotNull(id, "id must not be null");
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
	 * Returns the Sone this image belongs to.
	 *
	 * @return The Sone this image belongs to
	 */
	public Sone getSone() {
		return sone;
	}

	/**
	 * Sets the owner of this image. The owner can only be set if no owner has
	 * yet been set.
	 *
	 * @param sone
	 *            The new owner of this image
	 * @return This image
	 */
	public Image setSone(Sone sone) {
		checkNotNull(sone, "sone must not be null");
		checkArgument((this.sone == null) || this.sone.equals(sone), "sone must not already be set to another sone");
		this.sone = sone;
		return this;
	}

	/**
	 * Returns the album this image belongs to.
	 *
	 * @return The album this image belongs to
	 */
	public Album getAlbum() {
		return album;
	}

	/**
	 * Sets the album this image belongs to. The album of an image can only be
	 * set once, and it is usually called by {@link Album#addImage(Image)}.
	 *
	 * @param album
	 *            The album this image belongs to
	 * @return This image
	 */
	public Image setAlbum(Album album) {
		checkNotNull(album, "album must not be null");
		checkNotNull(album.getSone().equals(getSone()), "album must belong to the same Sone as this image");
		this.album = album;
		return this;
	}

	/**
	 * Returns the request key of this image.
	 *
	 * @return The request key of this image
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Sets the request key of this image. The request key can only be set as
	 * long as no request key has yet been set.
	 *
	 * @param key
	 *            The new request key of this image
	 * @return This image
	 */
	public Image setKey(String key) {
		checkNotNull(key, "key must not be null");
		checkState((this.key == null) || this.key.equals(key), "key must not be already set to another key");
		this.key = key;
		return this;
	}

	/**
	 * Returns whether the image has already been inserted. An image is
	 * considered as having been inserted it its {@link #getKey() key} is not
	 * {@code null}.
	 *
	 * @return {@code true} if there is a key for this image, {@code false}
	 *         otherwise
	 */
	public boolean isInserted() {
		return key != null;
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
	 * Sets the new creation time of this image. The creation time can only be
	 * set as long as no creation time has been set yet.
	 *
	 * @param creationTime
	 *            The new creation time of this image
	 * @return This image
	 */
	public Image setCreationTime(long creationTime) {
		checkArgument(creationTime > 0, "creationTime must be > 0");
		checkState((this.creationTime == 0) || (this.creationTime == creationTime), "creationTime must not already be set");
		this.creationTime = creationTime;
		return this;
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
	 * Sets the width of this image. The width can only be set as long as no
	 * width has been set yet.
	 *
	 * @param width
	 *            The new width of this image
	 * @return This image
	 */
	public Image setWidth(int width) {
		checkArgument(width > 0, "width must be > 0");
		checkState((this.width == 0) || (this.width == width), "width must not already be set to another width");
		this.width = width;
		return this;
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
	 * Sets the new height of this image. The height can only be set as long as
	 * no height has yet been set.
	 *
	 * @param height
	 *            The new height of this image
	 * @return This image
	 */
	public Image setHeight(int height) {
		checkArgument(height > 0, "height must be > 0");
		checkState((this.height == 0) || (this.height == height), "height must not already be set to another height");
		this.height = height;
		return this;
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
		this.title = checkNotNull(title, "title must not be null");
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
		this.description = checkNotNull(description, "description must not be null");
		return this;
	}

	//
	// FINGERPRINTABLE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFingerprint() {
		StringBuilder fingerprint = new StringBuilder();
		fingerprint.append("Image(");
		fingerprint.append("ID(").append(id).append(')');
		fingerprint.append("Title(").append(title).append(')');
		fingerprint.append("Description(").append(description).append(')');
		fingerprint.append(')');
		return fingerprint.toString();
	}

	//
	// OBJECT METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Image)) {
			return false;
		}
		return ((Image) object).id.equals(id);
	}

}
