/*
 * Sone - PostReply.java - Copyright © 2010–2012 David Roden
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
public class PostReply extends Reply<PostReply> {

	/** The Post this reply refers to. */
	private volatile Post post;

	/**
	 * Creates a new reply.
	 *
	 * @param id
	 *            The ID of the reply
	 */
	public PostReply(String id) {
		this(id, null, null, 0, null);
	}

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
	public PostReply(Sone sone, Post post, String text) {
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
	public PostReply(Sone sone, Post post, long time, String text) {
		this(UUID.randomUUID().toString(), sone, post, time, text);
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
	public PostReply(String id, Sone sone, Post post, long time, String text) {
		super(id, sone, time, text);
		this.post = post;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the post this reply refers to.
	 *
	 * @return The post this reply refers to
	 */
	public Post getPost() {
		return post;
	}

	/**
	 * Sets the post this reply refers to.
	 *
	 * @param post
	 *            The post this reply refers to
	 * @return This reply (for method chaining)
	 */
	public PostReply setPost(Post post) {
		this.post = post;
		return this;
	}

}
