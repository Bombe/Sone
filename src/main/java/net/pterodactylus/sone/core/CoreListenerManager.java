/*
 * Sone - CoreListenerManager.java - Copyright © 2010–2012 David Roden
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

import net.pterodactylus.sone.data.Image;
import net.pterodactylus.util.event.AbstractListenerManager;

/**
 * Manager for {@link CoreListener}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CoreListenerManager extends AbstractListenerManager<Core, CoreListener> {

	/**
	 * Creates a new core listener manager.
	 *
	 * @param source
	 *            The Core
	 */
	public CoreListenerManager(Core source) {
		super(source);
	}

	//
	// ACTIONS
	//

	/**
	 * Notifies all listeners that an image has started being inserted.
	 *
	 * @see CoreListener#imageInsertStarted(Image)
	 * @param image
	 *            The image that is now inserted
	 */
	void fireImageInsertStarted(Image image) {
		for (CoreListener coreListener : getListeners()) {
			coreListener.imageInsertStarted(image);
		}
	}

	/**
	 * Notifies all listeners that an image insert was aborted by the user.
	 *
	 * @see CoreListener#imageInsertAborted(Image)
	 * @param image
	 *            The image that is not inserted anymore
	 */
	void fireImageInsertAborted(Image image) {
		for (CoreListener coreListener : getListeners()) {
			coreListener.imageInsertAborted(image);
		}
	}

	/**
	 * Notifies all listeners that an image was successfully inserted.
	 *
	 * @see CoreListener#imageInsertFinished(Image)
	 * @param image
	 *            The image that was inserted
	 */
	void fireImageInsertFinished(Image image) {
		for (CoreListener coreListener : getListeners()) {
			coreListener.imageInsertFinished(image);
		}
	}

	/**
	 * Notifies all listeners that an image failed to be inserted.
	 *
	 * @see CoreListener#imageInsertFailed(Image, Throwable)
	 * @param image
	 *            The image that could not be inserted
	 * @param cause
	 *            The cause of the failure
	 */
	void fireImageInsertFailed(Image image, Throwable cause) {
		for (CoreListener coreListener : getListeners()) {
			coreListener.imageInsertFailed(image, cause);
		}
	}

}
