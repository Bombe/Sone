/*
 * Sone - ImageInsertListener.java - Copyright © 2011–2012 David Roden
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

import net.pterodactylus.sone.core.FreenetInterface.InsertToken;
import net.pterodactylus.sone.data.Image;
import freenet.keys.FreenetURI;

/**
 * Listener interface for objects that want to be notified about the status of
 * an image insert.
 *
 * @see ImageInserter#insertImage(net.pterodactylus.sone.data.TemporaryImage,
 *      Image)
 * @see FreenetInterface#insertImage(net.pterodactylus.sone.data.TemporaryImage,
 *      Image, net.pterodactylus.sone.core.FreenetInterface.InsertToken)
 * @see InsertToken
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface ImageInsertListener extends EventListener {

	/**
	 * Notifies a listener that the insert of the given image started.
	 *
	 * @param image
	 *            The image that is being inserted
	 */
	public void imageInsertStarted(Image image);

	/**
	 * Notifies a listener that the insert of the given image was aborted by the
	 * user.
	 *
	 * @param image
	 *            The image that is no longer being inserted
	 */
	public void imageInsertAborted(Image image);

	/**
	 * Notifies a listener that the given image was inserted successfully.
	 *
	 * @param image
	 *            The image that was inserted
	 * @param key
	 *            The final key of the image
	 */
	public void imageInsertFinished(Image image, FreenetURI key);

	/**
	 * Notifies a listener that the given image could not be inserted.
	 *
	 * @param image
	 *            The image that could not be inserted
	 * @param cause
	 *            The cause of the insertion failure
	 */
	public void imageInsertFailed(Image image, Throwable cause);

}
