/*
 * Sone - ImageImpl.java - Copyright © 2011–2013 David Roden
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

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.UUID;

import com.google.common.base.Optional;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/**
 * Container for image metadata.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ImageImpl implements Image {

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

	/** Creates a new image with a random ID. */
	public ImageImpl() {
		this(UUID.randomUUID().toString());
		this.creationTime = System.currentTimeMillis();
	}

	/**
	 * Creates a new image.
	 *
	 * @param id
	 * 		The ID of the image
	 */
	public ImageImpl(String id) {
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
	public Album getAlbum() {
		return album;
	}

	@Override
	public Image setAlbum(Album album) {
		checkNotNull(album, "album must not be null");
		checkNotNull(album.getSone().equals(getSone()), "album must belong to the same Sone as this image");
		this.album = album;
		return this;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public boolean isInserted() {
		return key != null;
	}

	@Override
	public long getCreationTime() {
		return creationTime;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public Modifier modify() throws IllegalStateException {
		checkState((sone == null) || sone.isLocal(), "only local images may be modified");
		return new Modifier() {
			private Optional<Sone> sone = absent();

			private Optional<Long> creationTime = absent();

			private Optional<String> key = absent();

			private Optional<String> title = absent();

			private Optional<String> description = absent();

			private Optional<Integer> width = absent();

			private Optional<Integer> height = absent();

			@Override
			public Modifier setSone(Sone sone) {
				this.sone = fromNullable(sone);
				return this;
			}

			@Override
			public Modifier setCreationTime(long creationTime) {
				this.creationTime = of(creationTime);
				return this;
			}

			@Override
			public Modifier setKey(String key) {
				this.key = fromNullable(key);
				return this;
			}

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
			public Modifier setWidth(int width) {
				this.width = of(width);
				return this;
			}

			@Override
			public Modifier setHeight(int height) {
				this.height = of(height);
				return this;
			}

			@Override
			public Image update() throws IllegalStateException {
				checkState(!sone.isPresent() || sone.get().equals(ImageImpl.this.sone), "can not change Sone once set");
				checkState(!creationTime.isPresent() || (ImageImpl.this.creationTime == 0), "can not change creation time once set");
				checkState(!key.isPresent() || key.get().equals(ImageImpl.this.key), "can not change key once set");
				checkState(!width.isPresent() || width.get().equals(ImageImpl.this.width), "can not change width once set");
				checkState(!height.isPresent() || height.get().equals(ImageImpl.this.height), "can not change height once set");

				ImageImpl.this.sone = sone.or(ImageImpl.this.sone);
				ImageImpl.this.creationTime = creationTime.or(ImageImpl.this.creationTime);
				ImageImpl.this.key = key.or(ImageImpl.this.key);
				ImageImpl.this.title = title.or(ImageImpl.this.title);
				ImageImpl.this.description = description.or(ImageImpl.this.description);
				ImageImpl.this.width = width.or(ImageImpl.this.width);
				ImageImpl.this.height = height.or(ImageImpl.this.height);

				return ImageImpl.this;
			}
		};
	}

	//
	// FINGERPRINTABLE METHODS
	//

	@Override
	public String getFingerprint() {
		Hasher hash = Hashing.sha256().newHasher();
		hash.putString("Image(");
		hash.putString("ID(").putString(id).putString(")");
		hash.putString("Title(").putString(title).putString(")");
		hash.putString("Description(").putString(description).putString(")");
		hash.putString(")");
		return hash.hash().toString();
	}

	//
	// OBJECT METHODS
	//

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ImageImpl)) {
			return false;
		}
		return ((ImageImpl) object).id.equals(id);
	}

}
