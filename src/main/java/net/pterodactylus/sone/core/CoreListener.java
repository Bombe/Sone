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

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;

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

}
