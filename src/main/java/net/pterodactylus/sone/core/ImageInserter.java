/*
 * Sone - ImageInserter.java - Copyright © 2011–2012 David Roden
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.FreenetInterface.InsertToken;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.TemporaryImage;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.validation.Validation;

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
	private static final Logger logger = Logging.getLogger(ImageInserter.class);

	/** The core. */
	private final Core core;

	/** The freenet interface. */
	private final FreenetInterface freenetInterface;

	/** The tokens of running inserts. */
	private final Map<String, InsertToken> insertTokens = Collections.synchronizedMap(new HashMap<String, InsertToken>());

	/**
	 * Creates a new image inserter.
	 *
	 * @param core
	 *            The Sone core
	 * @param freenetInterface
	 *            The freenet interface
	 */
	public ImageInserter(Core core, FreenetInterface freenetInterface) {
		this.core = core;
		this.freenetInterface = freenetInterface;
	}

	/**
	 * Inserts the given image. The {@link #core} will automatically added as
	 * {@link ImageInsertListener} to the created {@link InsertToken}.
	 *
	 * @param temporaryImage
	 *            The temporary image data
	 * @param image
	 *            The image
	 */
	public void insertImage(TemporaryImage temporaryImage, Image image) {
		Validation.begin().isNotNull("Temporary Image", temporaryImage).isNotNull("Image", image).check().isEqual("Image IDs", image.getId(), temporaryImage.getId()).check();
		try {
			InsertToken insertToken = freenetInterface.new InsertToken(image);
			insertTokens.put(image.getId(), insertToken);
			insertToken.addImageInsertListener(core);
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
		insertToken.removeImageInsertListener(core);
	}

}
