/*
 * FreenetSone - StatusUpdate.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A post is a short message that a user writes in his Sone to let other users
 * know what is going on.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Post {

	/** The GUID of the post. */
	private final UUID id;

	/** The Sone this post belongs to. */
	private final Sone sone;

	/** The time of the post (in milliseconds since Jan 1, 1970 UTC). */
	private final long time;

	/** The text of the post. */
	private final String text;

	/** The replies that have been loaded for this post. */
	private final Set<Reply> replies = new HashSet<Reply>();

	/**
	 * Creates a new post.
	 *
	 * @param sone
	 *            The Sone this post belongs to
	 * @param text
	 *            The text of the post
	 */
	public Post(Sone sone, String text) {
		this(sone, System.currentTimeMillis(), text);
	}

	/**
	 * Creates a new post.
	 *
	 * @param sone
	 *            The Sone this post belongs to
	 * @param time
	 *            The time of the post (in milliseconds since Jan 1, 1970 UTC)
	 * @param text
	 *            The text of the post
	 */
	public Post(Sone sone, long time, String text) {
		this(UUID.randomUUID(), sone, time, text);
	}

	/**
	 * Creates a new post.
	 *
	 * @param id
	 *            The ID of the post
	 * @param sone
	 *            The Sone this post belongs to
	 * @param time
	 *            The time of the post (in milliseconds since Jan 1, 1970 UTC)
	 * @param text
	 *            The text of the post
	 */
	public Post(UUID id, Sone sone, long time, String text) {
		this.id = id;
		this.sone = sone;
		this.time = time;
		this.text = text;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the ID of the post.
	 *
	 * @return The ID of the post
	 */
	public String getId() {
		return id.toString();
	}

	/**
	 * Returns the Sone this post belongs to.
	 *
	 * @return The Sone of this post
	 */
	public Sone getSone() {
		return sone;
	}

	/**
	 * Returns the time of the post.
	 *
	 * @return The time of the post (in milliseconds since Jan 1, 1970 UTC)
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Returns the text of the post.
	 *
	 * @return The text of the post
	 */
	public String getText() {
		return text;
	}

	/**
	 * Returns all replies to this post in unspecified order.
	 *
	 * @return All replies to this post
	 */
	public List<Reply> getReplies() {
		List<Reply> sortedReplies = new ArrayList<Reply>(replies);
		Collections.sort(sortedReplies, new Comparator<Reply>() {

			@Override
			public int compare(Reply leftReply, Reply rightReply) {
				return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, leftReply.getTime() - rightReply.getTime()));
			}

		});
		return sortedReplies;
	}

	/**
	 * Adds a reply to this post. The reply will not be added if its
	 * {@link Reply#getPost() post} is not equal to this post.
	 *
	 * @param reply
	 *            The reply to add
	 */
	public void addReply(Reply reply) {
		if (reply.getPost().equals(this)) {
			replies.add(reply);
		}
	}

	/**
	 * Removes a reply from this post.
	 *
	 * @param reply
	 *            The reply to remove
	 */
	public void removeReply(Reply reply) {
		if (reply.getPost().equals(this)) {
			replies.remove(reply);
		}
	}

	//
	// OBJECT METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return id.hashCode() ^ sone.hashCode() ^ (int) (time >> 32) ^ (int) (time & 0xffffffff) ^ text.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Post)) {
			return false;
		}
		Post post = (Post) object;
		return post.id.equals(id) && post.sone.equals(sone) && (post.time == time) && post.text.equals(text);
	}

}
