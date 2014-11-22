/*
 * Sone - Album.java - Copyright © 2011–2013 David Roden
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

package net.pterodactylus.sone.data.impl;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Sone;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/**
 * Container for images that can also contain nested {@link AlbumImpl}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class AlbumImpl implements Album {

	/** The ID of this album. */
	private final String id;

	/** The Sone this album belongs to. */
	private final Sone sone;

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

	/** Creates a new album with a random ID. */
	public AlbumImpl(Sone sone) {
		this(sone, UUID.randomUUID().toString());
	}

	/**
	 * Creates a new album with the given ID.
	 *
	 * @param id
	 * 		The ID of the album
	 */
	public AlbumImpl(Sone sone, String id) {
		this.sone = checkNotNull(sone, "Sone must not be null");
		this.id = checkNotNull(id, "id must not be null");
	}

	//
	// ACCESSORS
	//

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Sone getSone() {
		return sone;
	}

	@Override
	public List<Album> getAlbums() {
		return new ArrayList<Album>(albums);
	}

	@Override
	public void addAlbum(Album album) {
		checkNotNull(album, "album must not be null");
		checkArgument(album.getSone().equals(sone), "album must belong to the same Sone as this album");
		album.setParent(this);
		if (!albums.contains(album)) {
			albums.add(album);
		}
	}

	@Override
	public void removeAlbum(Album album) {
		checkNotNull(album, "album must not be null");
		checkArgument(album.getSone().equals(sone), "album must belong this album’s Sone");
		checkArgument(equals(album.getParent()), "album must belong to this album");
		albums.remove(album);
		album.removeParent();
	}

	@Override
	public Album moveAlbumUp(Album album) {
		checkNotNull(album, "album must not be null");
		checkArgument(album.getSone().equals(sone), "album must belong to the same Sone as this album");
		checkArgument(equals(album.getParent()), "album must belong to this album");
		int oldIndex = albums.indexOf(album);
		if (oldIndex <= 0) {
			return null;
		}
		albums.remove(oldIndex);
		albums.add(oldIndex - 1, album);
		return albums.get(oldIndex);
	}

	@Override
	public Album moveAlbumDown(Album album) {
		checkNotNull(album, "album must not be null");
		checkArgument(album.getSone().equals(sone), "album must belong to the same Sone as this album");
		checkArgument(equals(album.getParent()), "album must belong to this album");
		int oldIndex = albums.indexOf(album);
		if ((oldIndex < 0) || (oldIndex >= (albums.size() - 1))) {
			return null;
		}
		albums.remove(oldIndex);
		albums.add(oldIndex + 1, album);
		return albums.get(oldIndex);
	}

	@Override
	public List<Image> getImages() {
		return new ArrayList<Image>(Collections2.filter(Collections2.transform(imageIds, new Function<String, Image>() {

			@Override
			@SuppressWarnings("synthetic-access")
			public Image apply(String imageId) {
				return images.get(imageId);
			}
		}), Predicates.notNull()));
	}

	@Override
	public void addImage(Image image) {
		checkNotNull(image, "image must not be null");
		checkNotNull(image.getSone(), "image must have an owner");
		checkArgument(image.getSone().equals(sone), "image must belong to the same Sone as this album");
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

	@Override
	public void removeImage(Image image) {
		checkNotNull(image, "image must not be null");
		checkNotNull(image.getSone(), "image must have an owner");
		checkArgument(image.getSone().equals(sone), "image must belong to the same Sone as this album");
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

	@Override
	public Image moveImageUp(Image image) {
		checkNotNull(image, "image must not be null");
		checkNotNull(image.getSone(), "image must have an owner");
		checkArgument(image.getSone().equals(sone), "image must belong to the same Sone as this album");
		checkArgument(image.getAlbum().equals(this), "image must belong to this album");
		int oldIndex = imageIds.indexOf(image.getId());
		if (oldIndex <= 0) {
			return null;
		}
		imageIds.remove(image.getId());
		imageIds.add(oldIndex - 1, image.getId());
		return images.get(imageIds.get(oldIndex));
	}

	@Override
	public Image moveImageDown(Image image) {
		checkNotNull(image, "image must not be null");
		checkNotNull(image.getSone(), "image must have an owner");
		checkArgument(image.getSone().equals(sone), "image must belong to the same Sone as this album");
		checkArgument(image.getAlbum().equals(this), "image must belong to this album");
		int oldIndex = imageIds.indexOf(image.getId());
		if ((oldIndex == -1) || (oldIndex >= (imageIds.size() - 1))) {
			return null;
		}
		imageIds.remove(image.getId());
		imageIds.add(oldIndex + 1, image.getId());
		return images.get(imageIds.get(oldIndex));
	}

	@Override
	public Image getAlbumImage() {
		if (albumImage == null) {
			return null;
		}
		return Optional.fromNullable(images.get(albumImage)).or(images.values().iterator().next());
	}

	@Override
	public boolean isEmpty() {
		return albums.isEmpty() && images.isEmpty();
	}

	@Override
	public boolean isRoot() {
		return parent == null;
	}

	@Override
	public Album getParent() {
		return parent;
	}

	@Override
	public Album setParent(Album parent) {
		this.parent = checkNotNull(parent, "parent must not be null");
		return this;
	}

	@Override
	public Album removeParent() {
		this.parent = null;
		return this;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Modifier modify() throws IllegalStateException {
		// TODO: reenable check for local Sones
		return new Modifier() {
			private Optional<String> title = absent();

			private Optional<String> description = absent();

			private Optional<String> albumImage = absent();

			@Override
			public Modifier setTitle(String title) {
				this.title = fromNullable(title);
				return this;
			}

			@Override
			public Modifier setDescription(String description) {
				this.description = fromNullable(description);
				return this;
			}

			@Override
			public Modifier setAlbumImage(String imageId) {
				this.albumImage = fromNullable(imageId);
				return this;
			}

			@Override
			public Album update() throws IllegalStateException {
				if (title.isPresent() && title.get().trim().isEmpty()) {
					throw new AlbumTitleMustNotBeEmpty();
				}
				if (title.isPresent()) {
					AlbumImpl.this.title = title.get();
				}
				if (description.isPresent()) {
					AlbumImpl.this.description = description.get();
				}
				if (albumImage.isPresent()) {
					AlbumImpl.this.albumImage = albumImage.get();
				}
				return AlbumImpl.this;
			}
		};
	}

	//
	// FINGERPRINTABLE METHODS
	//

	@Override
	public String getFingerprint() {
		Hasher hash = Hashing.sha256().newHasher();
		hash.putString("Album(");
		hash.putString("ID(").putString(id).putString(")");
		hash.putString("Title(").putString(title).putString(")");
		hash.putString("Description(").putString(description).putString(")");
		if (albumImage != null) {
			hash.putString("AlbumImage(").putString(albumImage).putString(")");
		}

		/* add nested albums. */
		hash.putString("Albums(");
		for (Album album : albums) {
			hash.putString(album.getFingerprint());
		}
		hash.putString(")");

		/* add images. */
		hash.putString("Images(");
		for (Image image : getImages()) {
			if (image.isInserted()) {
				hash.putString(image.getFingerprint());
			}
		}
		hash.putString(")");

		hash.putString(")");
		return hash.hash().toString();
	}

	//
	// OBJECT METHODS
	//

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof AlbumImpl)) {
			return false;
		}
		AlbumImpl album = (AlbumImpl) object;
		return id.equals(album.id);
	}

}
