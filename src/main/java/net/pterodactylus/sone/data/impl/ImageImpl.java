/*
 * Sone - ImageImpl.java - Copyright © 2011–2020 David Roden
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

import java.util.*;
import javax.annotation.*;

import com.google.common.hash.*;
import net.pterodactylus.sone.data.*;

import static com.google.common.base.Preconditions.*;
import static java.nio.charset.StandardCharsets.*;

/**
 * Container for image metadata.
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
		checkState(album.getSone().equals(getSone()), "album must belong to the same Sone as this image");
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
		// TODO: reenable check for local images
		return new Modifier() {
			@Nullable
			private Sone sone;
			@Nullable
			private Long creationTime;
			@Nullable
			private String key;
			@Nullable
			private String title;
			@Nullable
			private String description;
			@Nullable
			private Integer width;
			@Nullable
			private Integer height;

			@Override
			public Modifier setSone(Sone sone) {
				this.sone = sone;
				return this;
			}

			@Override
			public Modifier setCreationTime(long creationTime) {
				this.creationTime = creationTime;
				return this;
			}

			@Override
			public Modifier setKey(String key) {
				this.key = key;
				return this;
			}

			@Override
			public Modifier setTitle(String title) {
				this.title = title;
				return this;
			}

			@Override
			public Modifier setDescription(String description) {
				this.description = description;
				return this;
			}

			@Override
			public Modifier setWidth(int width) {
				this.width = width;
				return this;
			}

			@Override
			public Modifier setHeight(int height) {
				this.height = height;
				return this;
			}

			@Override
			public Image update() throws IllegalStateException {
				checkState(sone == null || (ImageImpl.this.sone == null) || sone.equals(ImageImpl.this.sone), "can not change Sone once set");
				checkState(creationTime == null || ((ImageImpl.this.creationTime == 0) || (ImageImpl.this.creationTime == creationTime)), "can not change creation time once set");
				checkState(key == null || (ImageImpl.this.key == null) || key.equals(ImageImpl.this.key), "can not change key once set");
				if (title != null && title.trim().isEmpty()) {
					throw new ImageTitleMustNotBeEmpty();
				}
				checkState(width == null || (ImageImpl.this.width == 0) || width.equals(ImageImpl.this.width), "can not change width once set");
				checkState(height == null || (ImageImpl.this.height == 0) || height.equals(ImageImpl.this.height), "can not change height once set");

				if (sone != null) {
					ImageImpl.this.sone = sone;
				}
				if (creationTime != null) {
					ImageImpl.this.creationTime = creationTime;
				}
				if (key != null) {
					ImageImpl.this.key = key;
				}
				if (title != null) {
					ImageImpl.this.title = title;
				}
				if (description != null) {
					ImageImpl.this.description = description;
				}
				if (width != null) {
					ImageImpl.this.width = width;
				}
				if (height != null) {
					ImageImpl.this.height = height;
				}

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
		hash.putString("Image(", UTF_8);
		hash.putString("ID(", UTF_8).putString(id, UTF_8).putString(")", UTF_8);
		hash.putString("Title(", UTF_8).putString(title, UTF_8).putString(")", UTF_8);
		hash.putString("Description(", UTF_8).putString(description, UTF_8).putString(")", UTF_8);
		hash.putString(")", UTF_8);
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
