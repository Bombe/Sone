/*
 * Sone - CoreListener.java - Copyright © 2010–2012 David Roden
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

import java.util.EventListener;

import net.pterodactylus.sone.data.Image;

/**
 * Listener interface for objects that want to be notified on certain
 * {@link Core} events, such es discovery of new data.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface CoreListener extends EventListener {

	/**
	 * Notifies a listener that an image has started being inserted.
	 *
	 * @param image
	 *            The image that is now inserted
	 */
	public void imageInsertStarted(Image image);

	/**
	 * Notifies a listener that an image insert was aborted by the user.
	 *
	 * @param image
	 *            The image that is not inserted anymore
	 */
	public void imageInsertAborted(Image image);

	/**
	 * Notifies a listener that an image was successfully inserted.
	 *
	 * @param image
	 *            The image that was inserted
	 */
	public void imageInsertFinished(Image image);

	/**
	 * Notifies a listener that an image failed to be inserted.
	 *
	 * @param image
	 *            The image that could not be inserted
	 * @param cause
	 *            The reason for the failed insert
	 */
	public void imageInsertFailed(Image image, Throwable cause);

}
