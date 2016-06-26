/*
 * Sone - ImageInsertFailedEvent.java - Copyright © 2013–2016 David Roden
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

/**
 * Event that signals that an {@link Image} insert has failed.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ImageInsertFailedEvent extends ImageEvent {

	/** The cause of the insert failure. */
	private final Throwable cause;

	/**
	 * Creates a new “image insert failed” event.
	 *
	 * @param image
	 *            The image whose insert failed
	 * @param cause
	 *            The cause of the insert failure
	 */
	public ImageInsertFailedEvent(Image image, Throwable cause) {
		super(image);
		this.cause = cause;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the cause of the insert failure.
	 *
	 * @return The cause of the insert failure
	 */
	public Throwable cause() {
		return cause;
	}

}
