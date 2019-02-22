/*
 * Sone - Album.java - Copyright © 2011–2016 David Roden
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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

/**
 * Container for images that can also contain nested {@link Album}s.
 */
public interface Album extends Identified, Fingerprintable {

	/** Function that flattens the given album and all albums beneath it. */
	Function<Album, List<Album>> FLATTENER = new Function<Album, List<Album>>() {

		@Override
		@Nonnull
		public List<Album> apply(Album album) {
			if (album == null) {
				return emptyList();
			}
			List<Album> albums = new ArrayList<>();
			albums.add(album);
			for (Album subAlbum : album.getAlbums()) {
				albums.addAll(FluentIterable.from(ImmutableList.of(subAlbum)).transformAndConcat(FLATTENER).toList());
			}
			return albums;
		}
	};

	/** Function that transforms an album into the images it contains. */
	Function<Album, List<Image>> IMAGES = new Function<Album, List<Image>>() {

		@Override
		@Nonnull
		public List<Image> apply(Album album) {
			return (album != null) ? album.getImages() : Collections.<Image>emptyList();
		}
	};

	/**
	 * Filter that removes all albums that do not have any images in any album
	 * below it.
	 */
	Predicate<Album> NOT_EMPTY = new Predicate<Album>() {

		@Override
		public boolean apply(Album album) {
			/* so, we flatten all albums below the given one and check whether at least one album… */
			return FluentIterable.from(asList(album)).transformAndConcat(FLATTENER).anyMatch(new Predicate<Album>() {

				@Override
				public boolean apply(Album album) {
					/* …contains any inserted images. */
					return !album.getImages().isEmpty() && FluentIterable.from(album.getImages()).allMatch(new Predicate<Image>() {

						@Override
						public boolean apply(Image input) {
							return input.isInserted();
						}
					});
				}
			});
		}
	};

	/**
	 * Returns the ID of this album.
	 *
	 * @return The ID of this album
	 */
	String getId();

	/**
	 * Returns the Sone this album belongs to.
	 *
	 * @return The Sone this album belongs to
	 */
	Sone getSone();

	/**
	 * Returns the nested albums.
	 *
	 * @return The nested albums
	 */
	List<Album> getAlbums();

	/**
	 * Adds an album to this album.
	 *
	 * @param album
	 * 		The album to add
	 */
	void addAlbum(Album album);

	/**
	 * Removes an album from this album.
	 *
	 * @param album
	 * 		The album to remove
	 */
	void removeAlbum(Album album);

	/**
	 * Moves the given album up in this album’s albums. If the album is already the
	 * first album, nothing happens.
	 *
	 * @param album
	 * 		The album to move up
	 * @return The album that the given album swapped the place with, or
	 *         <code>null</code> if the album did not change its place
	 */
	Album moveAlbumUp(Album album);

	/**
	 * Moves the given album down in this album’s albums. If the album is already
	 * the last album, nothing happens.
	 *
	 * @param album
	 * 		The album to move down
	 * @return The album that the given album swapped the place with, or
	 *         <code>null</code> if the album did not change its place
	 */
	Album moveAlbumDown(Album album);

	/**
	 * Returns the images in this album.
	 *
	 * @return The images in this album
	 */
	List<Image> getImages();

	/**
	 * Adds the given image to this album.
	 *
	 * @param image
	 * 		The image to add
	 */
	void addImage(Image image);

	/**
	 * Removes the given image from this album.
	 *
	 * @param image
	 * 		The image to remove
	 */
	void removeImage(Image image);

	/**
	 * Moves the given image up in this album’s images. If the image is already the
	 * first image, nothing happens.
	 *
	 * @param image
	 * 		The image to move up
	 * @return The image that the given image swapped the place with, or
	 *         <code>null</code> if the image did not change its place
	 */
	Image moveImageUp(Image image);

	/**
	 * Moves the given image down in this album’s images. If the image is already
	 * the last image, nothing happens.
	 *
	 * @param image
	 * 		The image to move down
	 * @return The image that the given image swapped the place with, or
	 *         <code>null</code> if the image did not change its place
	 */
	Image moveImageDown(Image image);

	/**
	 * Returns whether this album contains any other albums or images.
	 *
	 * @return {@code true} if this album is empty, {@code false} otherwise
	 */
	boolean isEmpty();

	/**
	 * Returns whether this album is an identitiy’s root album.
	 *
	 * @return {@code true} if this album is an identity’s root album, {@code
	 *         false} otherwise
	 */
	boolean isRoot();

	/**
	 * Returns the parent album of this album.
	 *
	 * @return The parent album of this album, or {@code null} if this album does
	 *         not have a parent
	 */
	Album getParent();

	/**
	 * Sets the parent album of this album.
	 *
	 * @param parent
	 * 		The new parent album of this album
	 * @return This album
	 */
	Album setParent(Album parent);

	/**
	 * Removes the parent album of this album.
	 *
	 * @return This album
	 */
	Album removeParent();

	/**
	 * Returns the title of this album.
	 *
	 * @return The title of this album
	 */
	String getTitle();

	/**
	 * Returns the description of this album.
	 *
	 * @return The description of this album
	 */
	String getDescription();

	/**
	 * Returns a modifier for this album.
	 *
	 * @return A modifier for this album
	 * @throws IllegalStateException
	 * 		if this album can not be modified
	 */
	Modifier modify() throws IllegalStateException;

	/**
	 * Allows modifying an album. Modifications are only performed once {@link
	 * #update()} has succesfully returned a new album with the modifications
	 * made.
	 */
	interface Modifier {

		Modifier setTitle(String title);

		Modifier setDescription(String description);

		Album update() throws IllegalStateException;

		class AlbumTitleMustNotBeEmpty extends IllegalStateException { }

	}

}
