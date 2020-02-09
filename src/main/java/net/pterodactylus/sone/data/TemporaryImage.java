/*
 * Sone - TemporaryImage.java - Copyright © 2011–2020 David Roden
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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.UUID;

/**
 * A temporary image stores an uploaded image in memory until it has been
 * inserted into Freenet and is subsequently loaded from there.
 */
public class TemporaryImage {

	/** The ID of the temporary image. */
	private final String id;

	/** The MIME type of the image. */
	private String mimeType;

	/** The encoded image data. */
	private byte[] imageData;

	/**
	 * Creates a new temporary image with a random ID.
	 */
	public TemporaryImage() {
		this(UUID.randomUUID().toString());
	}

	/**
	 * Creates a new temporary image.
	 *
	 * @param id
	 *            The ID of the temporary image
	 */
	public TemporaryImage(String id) {
		this.id = id;
	}

	/**
	 * Returns the ID of the temporary image.
	 *
	 * @return The ID of the temporary image
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the MIME type of the image.
	 *
	 * @return The MIME type of the image
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Sets the MIME type of the image. The MIME type can only be set once and
	 * it must not be {@code null}.
	 *
	 * @param mimeType
	 *            The MIME type of the image
	 * @return This temporary image
	 */
	public TemporaryImage setMimeType(String mimeType) {
		checkNotNull(mimeType, "mimeType must not be null");
		checkState(this.mimeType == null, "mime type must not already be set");
		this.mimeType = mimeType;
		return this;
	}

	/**
	 * Returns the encoded image data.
	 *
	 * @return The encoded image data
	 */
	public byte[] getImageData() {
		return imageData;
	}

	/**
	 * Sets the encoded image data. The encoded image data can only be set once
	 * and it must not be {@code null}.
	 *
	 * @param imageData
	 *            The encoded image data
	 * @return This temporary image
	 */
	public TemporaryImage setImageData(byte[] imageData) {
		checkNotNull(imageData, "imageData must not be null");
		checkState(this.imageData == null, "image data must not already be set");
		this.imageData = imageData;
		return this;
	}

}
