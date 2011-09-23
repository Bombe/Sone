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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.pterodactylus.util.collection.Mapper;
import net.pterodactylus.util.collection.Mappers;
import net.pterodactylus.util.object.Default;
import net.pterodactylus.util.validation.Validation;

/**
 * Container for images that can also contain nested {@link Album}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Album implements Fingerprintable {

	/** The ID of this album. */
	private final String id;

	/** The Sone this album belongs to. */
	private Sone sone;

	/** Nested albums. */
	private final List<Album> albums = new ArrayList<Album>();

	/** The image IDs in order. */
	private final List<String> imageIds = new ArrayList<String>();

	/** The images in this album. */
	private final Map<String, Image> images = new HashMap<String, Image>();

	/** The parent album. */
	private Album parent;

	/** The title of this album. */
	private String title;

	/** The description of this album. */
	private String description;

	/** The ID of the album picture. */
	private String albumImage;

	/**
	 * Creates a new album with a random ID.
	 */
	public Album() {
		this(UUID.randomUUID().toString());
	}

	/**
	 * Creates a new album with the given ID.
	 *
	 * @param id
	 *            The ID of the album
	 */
	public Album(String id) {
		Validation.begin().isNotNull("Album ID", id).check();
		this.id = id;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the ID of this album.
	 *
	 * @return The ID of this album
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the Sone this album belongs to.
	 *
	 * @return The Sone this album belongs to
	 */
	public Sone getSone() {
		return sone;
	}

	/**
	 * Sets the owner of the album. The owner can only be set as long as the
	 * current owner is {@code null}.
	 *
	 * @param sone
	 *            The album owner
	 * @return This album
	 */
	public Album setSone(Sone sone) {
		Validation.begin().isNotNull("New Album Owner", sone).isEither("Old Album Owner", this.sone, null, sone).check();
		this.sone = sone;
		return this;
	}

	/**
	 * Returns the nested albums.
	 *
	 * @return The nested albums
	 */
	public List<Album> getAlbums() {
		return new ArrayList<Album>(albums);
	}

	/**
	 * Adds an album to this album.
	 *
	 * @param album
	 *            The album to add
	 */
	public void addAlbum(Album album) {
		Validation.begin().isNotNull("Album", album).check().isEqual("Album Owner", album.sone, sone).isEither("Old Album Parent", this.parent, null, album.parent).check();
		album.setParent(this);
		if (!albums.contains(album)) {
			albums.add(album);
		}
	}

	/**
	 * Removes an album from this album.
	 *
	 * @param album
	 *            The album to remove
	 */
	public void removeAlbum(Album album) {
		Validation.begin().isNotNull("Album", album).check().isEqual("Album Owner", album.sone, sone).isEqual("Album Parent", album.parent, this).check();
		albums.remove(album);
		album.removeParent();
	}

	/**
	 * Moves the given album up in this album’s albums. If the album is already
	 * the first album, nothing happens.
	 *
	 * @param album
	 *            The album to move up
	 * @return The album that the given album swapped the place with, or
	 *         <code>null</code> if the album did not change its place
	 */
	public Album moveAlbumUp(Album album) {
		Validation.begin().isNotNull("Album", album).check().isEqual("Album Owner", album.sone, sone).isEqual("Album Parent", album.parent, this).check();
		int oldIndex = albums.indexOf(album);
		if (oldIndex <= 0) {
			return null;
		}
		albums.remove(oldIndex);
		albums.add(oldIndex - 1, album);
		return albums.get(oldIndex);
	}

	/**
	 * Moves the given album down in this album’s albums. If the album is
	 * already the last album, nothing happens.
	 *
	 * @param album
	 *            The album to move down
	 * @return The album that the given album swapped the place with, or
	 *         <code>null</code> if the album did not change its place
	 */
	public Album moveAlbumDown(Album album) {
		Validation.begin().isNotNull("Album", album).check().isEqual("Album Owner", album.sone, sone).isEqual("Album Parent", album.parent, this).check();
		int oldIndex = albums.indexOf(album);
		if ((oldIndex < 0) || (oldIndex >= (albums.size() - 1))) {
			return null;
		}
		albums.remove(oldIndex);
		albums.add(oldIndex + 1, album);
		return albums.get(oldIndex);
	}

	/**
	 * Returns the images in this album.
	 *
	 * @return The images in this album
	 */
	public List<Image> getImages() {
		return Mappers.mappedList(imageIds, new Mapper<String, Image>() {

			@Override
			@SuppressWarnings("synthetic-access")
			public Image map(String imageId) {
				return images.get(imageId);
			}

		});
	}

	/**
	 * Adds the given image to this album.
	 *
	 * @param image
	 *            The image to add
	 */
	public void addImage(Image image) {
		Validation.begin().isNotNull("Image", image).check().isNotNull("Image Owner", image.getSone()).check().isEqual("Image Owner", image.getSone(), sone).check();
		if (image.getAlbum() != null) {
			image.getAlbum().removeImage(image);
		}
		image.setAlbum(this);
		if (imageIds.isEmpty() && (albumImage == null)) {
			albumImage = image.getId();
		}
		if (!imageIds.contains(image.getId())) {
			imageIds.add(image.getId());
			images.put(image.getId(), image);
		}
	}

	/**
	 * Removes the given image from this album.
	 *
	 * @param image
	 *            The image to remove
	 */
	public void removeImage(Image image) {
		Validation.begin().isNotNull("Image", image).check().isEqual("Image Owner", image.getSone(), sone).check();
		imageIds.remove(image.getId());
		images.remove(image.getId());
		if (image.getId().equals(albumImage)) {
			if (images.isEmpty()) {
				albumImage = null;
			} else {
				albumImage = images.values().iterator().next().getId();
			}
		}
	}

	/**
	 * Moves the given image up in this album’s images. If the image is already
	 * the first image, nothing happens.
	 *
	 * @param image
	 *            The image to move up
	 * @return The image that the given image swapped the place with, or
	 *         <code>null</code> if the image did not change its place
	 */
	public Image moveImageUp(Image image) {
		Validation.begin().isNotNull("Image", image).check().isEqual("Image Album", image.getAlbum(), this).isEqual("Album Owner", image.getAlbum().getSone(), sone).check();
		int oldIndex = imageIds.indexOf(image.getId());
		if (oldIndex <= 0) {
			return null;
		}
		imageIds.remove(image.getId());
		imageIds.add(oldIndex - 1, image.getId());
		return images.get(imageIds.get(oldIndex));
	}

	/**
	 * Move the given image down in this album’s images. If the image is already
	 * the last image, nothing happens.
	 *
	 * @param image
	 *            The image to move down
	 * @return The image that the given image swapped the place with, or
	 *         <code>null</code> if the image did not change its place
	 */
	public Image moveImageDown(Image image) {
		Validation.begin().isNotNull("Image", image).check().isEqual("Image Album", image.getAlbum(), this).isEqual("Album Owner", image.getAlbum().getSone(), sone).check();
		int oldIndex = imageIds.indexOf(image.getId());
		if ((oldIndex == -1) || (oldIndex >= (imageIds.size() - 1))) {
			return null;
		}
		imageIds.remove(image.getId());
		imageIds.add(oldIndex + 1, image.getId());
		return images.get(imageIds.get(oldIndex));
	}

	/**
	 * Returns the album image of this album, or {@code null} if no album image
	 * has been set.
	 *
	 * @return The image to show when this album is listed
	 */
	public Image getAlbumImage() {
		if (albumImage == null) {
			return null;
		}
		return Default.forNull(images.get(albumImage), images.values().iterator().next());
	}

	/**
	 * Sets the ID of the album image.
	 *
	 * @param id
	 *            The ID of the album image
	 * @return This album
	 */
	public Album setAlbumImage(String id) {
		this.albumImage = id;
		return this;
	}

	/**
	 * Returns whether this album contains any other albums or images.
	 *
	 * @return {@code true} if this album is empty, {@code false} otherwise
	 */
	public boolean isEmpty() {
		return albums.isEmpty() && images.isEmpty();
	}

	/**
	 * Returns the parent album of this album.
	 *
	 * @return The parent album of this album, or {@code null} if this album
	 *         does not have a parent
	 */
	public Album getParent() {
		return parent;
	}

	/**
	 * Sets the parent album of this album.
	 *
	 * @param parent
	 *            The new parent album of this album
	 * @return This album
	 */
	protected Album setParent(Album parent) {
		Validation.begin().isNotNull("Album Parent", parent).check();
		this.parent = parent;
		return this;
	}

	/**
	 * Removes the parent album of this album.
	 *
	 * @return This album
	 */
	protected Album removeParent() {
		this.parent = null;
		return this;
	}

	/**
	 * Returns the title of this album.
	 *
	 * @return The title of this album
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title of this album.
	 *
	 * @param title
	 *            The title of this album
	 * @return This album
	 */
	public Album setTitle(String title) {
		Validation.begin().isNotNull("Album Title", title).check();
		this.title = title;
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
		Validation.begin().isNotNull("Album Description", description).check();
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
		fingerprint.append("Album(");
		fingerprint.append("ID(").append(id).append(')');
		fingerprint.append("Title(").append(title).append(')');
		fingerprint.append("Description(").append(description).append(')');
		if (albumImage != null) {
			fingerprint.append("AlbumImage(").append(albumImage).append(')');
		}

		/* add nested albums. */
		fingerprint.append("Albums(");
		for (Album album : albums) {
			fingerprint.append(album.getFingerprint());
		}
		fingerprint.append(')');

		/* add images. */
		fingerprint.append("Images(");
		for (Image image : getImages()) {
			if (image.isInserted()) {
				fingerprint.append(image.getFingerprint());
			}
		}
		fingerprint.append(')');

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
		if (!(object instanceof Album)) {
			return false;
		}
		Album album = (Album) object;
		return id.equals(album.id);
	}

}
