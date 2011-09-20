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

import net.pterodactylus.util.validation.Validation;

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
		Validation.begin().isNotNull("Image ID", id).check();
		this.id = id;
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
		Validation.begin().isNotNull("New Image Owner", sone).isEither("Old Image Owner", this.sone, null, sone).check();
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
		Validation.begin().isNotNull("New Album", album).check().isEqual("Album Owner and Image Owner", album.getSone(), getSone()).check();
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
		Validation.begin().isNotNull("New Image Key", key).isEither("Old Image Key", this.key, null, key).check();
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
		Validation.begin().isGreater("New Image Creation Time", creationTime, 0).isEither("Old Image Creation Time", this.creationTime, 0L, creationTime).check();
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
		Validation.begin().isGreater("New Image Width", width, 0).isEither("Old Image Width", this.width, 0, width).check();
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
		Validation.begin().isGreater("New Image Height", height, 0).isEither("Old Image Height", this.height, 0, height).check();
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
		Validation.begin().isNotNull("Image Title", title).check();
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
		Validation.begin().isNotNull("Image Description", description).check();
		this.description = description;
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
