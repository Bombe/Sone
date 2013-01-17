/*
 * Sone - PostReplyImpl.java - Copyright © 2010–2013 David Roden
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

package net.pterodactylus.sone.data.impl;

import java.util.UUID;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;

/**
 * Simple {@link PostReply} implementation.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PostReplyImpl extends ReplyImpl<PostReply> implements PostReply {

	/** The Post this reply refers to. */
	private volatile Post post;

	/**
	 * Creates a new reply.
	 *
	 * @param id
	 *            The ID of the reply
	 */
	public PostReplyImpl(String id) {
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
	public PostReplyImpl(Sone sone, Post post, String text) {
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
	public PostReplyImpl(Sone sone, Post post, long time, String text) {
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
	public PostReplyImpl(String id, Sone sone, Post post, long time, String text) {
		super(id, sone, time, text);
		this.post = post;
	}

	//
	// ACCESSORS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	@Override
	public PostReply setPost(Post post) {
		this.post = post;
		return this;
	}

}
