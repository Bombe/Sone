/*
 * Sone - ImageInsertFinishedEvent.java - Copyright © 2013–2019 David Roden
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

package net.pterodactylus.sone.core.event;

import net.pterodactylus.sone.data.Image;
import freenet.keys.FreenetURI;

/**
 * Event that signals that an {@link Image} insert is finished.
 */
public class ImageInsertFinishedEvent extends ImageEvent {

	/** The URI of the image. */
	private final FreenetURI resultingUri;

	/**
	 * Creates a new “image insert finished” event.
	 *
	 * @param image
	 *            The image whose insert finished
	 * @param resultingUri
	 *            The resulting URI of the image
	 */
	public ImageInsertFinishedEvent(Image image, FreenetURI resultingUri) {
		super(image);
		this.resultingUri = resultingUri;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the URI of the image.
	 *
	 * @return The URI of the image
	 */
	public FreenetURI resultingUri() {
		return resultingUri;
	}

}
