/*
 * Sone - ImageInserter.java - Copyright © 2011–2015 David Roden
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

package net.pterodactylus.sone.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.logging.Logger.getLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.FreenetInterface.InsertToken;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.TemporaryImage;

import com.google.common.base.Function;

/**
 * The image inserter is responsible for inserting images using
 * {@link FreenetInterface#insertImage(TemporaryImage, Image, InsertToken)} and
 * also tracks running inserts, giving the possibility to abort a running
 * insert.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ImageInserter {

	/** The logger. */
	private static final Logger logger = getLogger(ImageInserter.class.getName());

	/** The freenet interface. */
	private final FreenetInterface freenetInterface;
	private final Function<Image, InsertToken> insertTokenSupplier;

	/** The tokens of running inserts. */
	private final Map<String, InsertToken> insertTokens = Collections.synchronizedMap(new HashMap<String, InsertToken>());

	/**
	 * Creates a new image inserter.
	 *
	 * @param freenetInterface
	 *            The freenet interface
	 * @param insertTokenSupplier
	 *            The supplier for insert tokens
	 */
	public ImageInserter(FreenetInterface freenetInterface, Function<Image, InsertToken> insertTokenSupplier) {
		this.freenetInterface = freenetInterface;
		this.insertTokenSupplier = insertTokenSupplier;
	}

	/**
	 * Inserts the given image.
	 *
	 * @param temporaryImage
	 *            The temporary image data
	 * @param image
	 *            The image
	 */
	public void insertImage(TemporaryImage temporaryImage, Image image) {
		checkNotNull(temporaryImage, "temporaryImage must not be null");
		checkNotNull(image, "image must not be null");
		checkArgument(image.getId().equals(temporaryImage.getId()), "image IDs must match");
		try {
			InsertToken insertToken = insertTokenSupplier.apply(image);
			insertTokens.put(image.getId(), insertToken);
			freenetInterface.insertImage(temporaryImage, image, insertToken);
		} catch (SoneException se1) {
			logger.log(Level.WARNING, "Could not insert image!", se1);
		}
	}

	/**
	 * Cancels a running image insert. If no insert is running for the given
	 * image, nothing happens.
	 *
	 * @param image
	 *            The image being inserted
	 */
	public void cancelImageInsert(Image image) {
		InsertToken insertToken = insertTokens.remove(image.getId());
		if (insertToken == null) {
			return;
		}
		insertToken.cancel();
	}

}
