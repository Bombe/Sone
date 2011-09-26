/*
 * Sone - CoreListener.java - Copyright © 2010 David Roden
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
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.version.Version;

/**
 * Listener interface for objects that want to be notified on certain
 * {@link Core} events, such es discovery of new data.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface CoreListener extends EventListener {

	/**
	 * Notifies a listener that a new Sone has been discovered.
	 *
	 * @param sone
	 *            The new Sone
	 */
	public void newSoneFound(Sone sone);

	/**
	 * Notifies a listener that a new post has been found.
	 *
	 * @param post
	 *            The new post
	 */
	public void newPostFound(Post post);

	/**
	 * Notifies a listener that a new reply has been found.
	 *
	 * @param reply
	 *            The new reply
	 */
	public void newReplyFound(Reply reply);

	/**
	 * Notifies a listener that the given Sone is now marked as known.
	 *
	 * @param sone
	 *            The known Sone
	 */
	public void markSoneKnown(Sone sone);

	/**
	 * Notifies a listener that the given post is now marked as known.
	 *
	 * @param post
	 *            The known post
	 */
	public void markPostKnown(Post post);

	/**
	 * Notifies a listener that the given reply is now marked as known.
	 *
	 * @param reply
	 *            The known reply
	 */
	public void markReplyKnown(Reply reply);

	/**
	 * Notifies a listener that the given Sone was removed.
	 *
	 * @param sone
	 *            The removed Sone
	 */
	public void soneRemoved(Sone sone);

	/**
	 * Notifies a listener that the given post was removed.
	 *
	 * @param post
	 *            The removed post
	 */
	public void postRemoved(Post post);

	/**
	 * Notifies a listener that the given reply was removed.
	 *
	 * @param reply
	 *            The removed reply
	 */
	public void replyRemoved(Reply reply);

	/**
	 * Notifies a listener when a Sone was locked.
	 *
	 * @param sone
	 *            The Sone that was locked
	 */
	public void soneLocked(Sone sone);

	/**
	 * Notifies a listener that a Sone was unlocked.
	 *
	 * @param sone
	 *            The Sone that was unlocked
	 */
	public void soneUnlocked(Sone sone);

	/**
	 * Notifies a listener that the insert of the given Sone has started.
	 *
	 * @see SoneInsertListener#insertStarted(Sone)
	 * @param sone
	 *            The Sone that is being inserted
	 */
	public void soneInserting(Sone sone);

	/**
	 * Notifies a listener that the insert of the given Sone has finished
	 * successfully.
	 *
	 * @see SoneInsertListener#insertFinished(Sone, long)
	 * @param sone
	 *            The Sone that has been inserted
	 * @param insertDuration
	 *            The insert duration (in milliseconds)
	 */
	public void soneInserted(Sone sone, long insertDuration);

	/**
	 * Notifies a listener that the insert of the given Sone was aborted.
	 *
	 * @see SoneInsertListener#insertAborted(Sone, Throwable)
	 * @param sone
	 *            The Sone that was inserted
	 * @param cause
	 *            The cause for the abortion (may be {@code null})
	 */
	public void soneInsertAborted(Sone sone, Throwable cause);

	/**
	 * Notifies a listener that a new version has been found.
	 *
	 * @param version
	 *            The version that was found
	 * @param releaseTime
	 *            The release time of the new version
	 * @param latestEdition
	 *            The latest edition of the Sone homepage
	 */
	public void updateFound(Version version, long releaseTime, long latestEdition);

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
