/*
 * Sone - ImageEvent.java - Copyright © 2013–2016 David Roden
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
 * Base class for {@link Image} events.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class ImageEvent {

	/** The image this event is about. */
	private final Image image;

	/**
	 * Creates a new image event.
	 *
	 * @param image
	 *            The image this event is about
	 */
	protected ImageEvent(Image image) {
		this.image = image;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the image this event is about.
	 *
	 * @return The image this event is about
	 */
	public Image image() {
		return image;
	}

}
