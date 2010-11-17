/*
 * Sone - CoreListenerManager.java - Copyright © 2010 David Roden
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

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
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
	 * Notifies all listeners that the given Sone is now being rescued.
	 *
	 * @see CoreListener#rescuingSone(Sone)
	 * @param sone
	 *            The Sone that is being rescued
	 */
	void fireRescuingSone(Sone sone) {
		for (CoreListener coreListener : getListeners()) {
			coreListener.rescuingSone(sone);
		}
	}

	/**
	 * Notifies all listeners that the given Sone was rescued.
	 *
	 * @see CoreListener#rescuedSone(Sone)
	 * @param sone
	 *            The Sone that was rescued
	 */
	void fireRescuedSone(Sone sone) {
		for (CoreListener coreListener : getListeners()) {
			coreListener.rescuedSone(sone);
		}
	}

	/**
	 * Notifies all listeners that a new Sone has been discovered.
	 *
	 * @see CoreListener#newSoneFound(Sone)
	 * @param sone
	 *            The discovered sone
	 */
	void fireNewSoneFound(Sone sone) {
		for (CoreListener coreListener : getListeners()) {
			coreListener.newSoneFound(sone);
		}
	}

	/**
	 * Notifies all listeners that a new post has been found.
	 *
	 * @see CoreListener#newPostFound(Post)
	 * @param post
	 *            The new post
	 */
	void fireNewPostFound(Post post) {
		for (CoreListener coreListener : getListeners()) {
			coreListener.newPostFound(post);
		}
	}

	/**
	 * Notifies all listeners that a new reply has been found.
	 *
	 * @see CoreListener#newReplyFound(Reply)
	 * @param reply
	 *            The new reply
	 */
	void fireNewReplyFound(Reply reply) {
		for (CoreListener coreListener : getListeners()) {
			coreListener.newReplyFound(reply);
		}
	}

	/**
	 * Notifies all listeners that the given Sone is now marked as known.
	 *
	 * @see CoreListener#markSoneKnown(Sone)
	 * @param sone
	 *            The known Sone
	 */
	void fireMarkSoneKnown(Sone sone) {
		for (CoreListener coreListener : getListeners()) {
			coreListener.markSoneKnown(sone);
		}
	}

	/**
	 * Notifies all listeners that the given post is now marked as known.
	 *
	 * @param post
	 *            The known post
	 */
	void fireMarkPostKnown(Post post) {
		for (CoreListener coreListener : getListeners()) {
			coreListener.markPostKnown(post);
		}
	}

	/**
	 * Notifies all listeners that the given reply is now marked as known.
	 *
	 * @param reply
	 *            The known reply
	 */
	void fireMarkReplyKnown(Reply reply) {
		for (CoreListener coreListener : getListeners()) {
			coreListener.markReplyKnown(reply);
		}
	}

}
