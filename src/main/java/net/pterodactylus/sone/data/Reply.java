/*
 * Sone - Reply.java - Copyright © 2010 David Roden
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

import java.util.UUID;

/**
 * A reply is like a {@link Post} but can never be posted on its own, it always
 * refers to another {@link Post}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Reply {

	/** The Sone that posted this reply. */
	private final Sone sone;

	/** The ID of the reply. */
	private final UUID id;

	/** The Post this reply refers to. */
	private final Post post;

	/** The time of the reply. */
	private final long time;

	/** The text of the reply. */
	private final String text;

	/**
	 * Creates a new reply.
	 *
	 * @param sone
	 *            The sone that posted the reply
	 * @param post
	 *            The post to reply to
	 * @param text
	 *            The text of the reply
	 */
	public Reply(Sone sone, Post post, String text) {
		this(sone, post, System.currentTimeMillis(), text);
	}

	/**
	 * Creates a new reply-
	 *
	 * @param sone
	 *            The sone that posted the reply
	 * @param post
	 *            The post to reply to
	 * @param time
	 *            The time of the reply
	 * @param text
	 *            The text of the reply
	 */
	public Reply(Sone sone, Post post, long time, String text) {
		this(sone, UUID.randomUUID(), post, time, text);
	}

	/**
	 * Creates a new reply-
	 *
	 * @param sone
	 *            The sone that posted the reply
	 * @param id
	 *            The ID of the reply
	 * @param post
	 *            The post to reply to
	 * @param time
	 *            The time of the reply
	 * @param text
	 *            The text of the reply
	 */
	public Reply(Sone sone, UUID id, Post post, long time, String text) {
		this.sone = sone;
		this.id = id;
		this.post = post;
		this.time = time;
		this.text = text;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the Sone that posted this reply.
	 *
	 * @return The Sone that posted this reply
	 */
	public Sone getSone() {
		return sone;
	}

	/**
	 * Returns the ID of the reply.
	 *
	 * @return The ID of the reply
	 */
	public String getId() {
		return id.toString();
	}

	/**
	 * Returns the post this reply refers to.
	 *
	 * @return The post this reply refers to
	 */
	public Post getPost() {
		return post;
	}

	/**
	 * Returns the time of the reply.
	 *
	 * @return The time of the reply (in milliseconds since Jan 1, 1970 UTC)
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Returns the text of the reply.
	 *
	 * @return The text of the reply
	 */
	public String getText() {
		return text;
	}

	//
	// OBJECT METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return post.hashCode() ^ id.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Reply)) {
			return false;
		}
		return ((Reply) object).post.equals(post) && ((Reply) object).id.equals(id);
	}

}
