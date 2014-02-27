/*
 * Sone - Image.java - Copyright © 2011–2013 David Roden
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

/**
 * Container for image metadata.
 *
 * @author <a href="mailto:d.roden@xplosion.de">David Roden</a>
 */
public interface Image extends Identified, Fingerprintable {

	/**
	 * Returns the ID of this image.
	 *
	 * @return The ID of this image
	 */
	String getId();

	/**
	 * Returns the Sone this image belongs to.
	 *
	 * @return The Sone this image belongs to
	 */
	Sone getSone();

	/**
	 * Returns the album this image belongs to.
	 *
	 * @return The album this image belongs to
	 */
	Album getAlbum();

	/**
	 * Sets the album this image belongs to. The album of an image can only be
	 * set once, and it is usually called by {@link Album#addImage(Image)}.
	 *
	 * @param album
	 *            The album this image belongs to
	 * @return This image
	 */
	Image setAlbum(Album album);

	/**
	 * Returns the request key of this image.
	 *
	 * @return The request key of this image
	 */
	String getKey();

	/**
	 * Returns whether the image has already been inserted. An image is
	 * considered as having been inserted it its {@link #getKey() key} is not
	 * {@code null}.
	 *
	 * @return {@code true} if there is a key for this image, {@code false}
	 *         otherwise
	 */
	boolean isInserted();

	/**
	 * Returns the creation time of this image.
	 *
	 * @return The creation time of this image (in milliseconds since 1970, Jan
	 *         1, UTC)
	 */
	long getCreationTime();

	/**
	 * Returns the width of this image.
	 *
	 * @return The width of this image (in pixels)
	 */
	int getWidth();

	/**
	 * Returns the height of this image.
	 *
	 * @return The height of this image (in pixels)
	 */
	int getHeight();

	/**
	 * Returns the title of this image.
	 *
	 * @return The title of this image
	 */
	String getTitle();

	/**
	 * Returns the description of this image.
	 *
	 * @return The description of this image
	 */
	String getDescription();

	/**
	 * {@inheritDoc}
	 */
	@Override
	String getFingerprint();

	Modifier modify() throws IllegalStateException;

	interface Modifier {

		Modifier setSone(Sone sone);

		Modifier setCreationTime(long creationTime);

		Modifier setKey(String key);

		Modifier setTitle(String title);

		Modifier setDescription(String description);

		Modifier setWidth(int width);

		Modifier setHeight(int height);

		Image update() throws IllegalStateException;

	}

}
