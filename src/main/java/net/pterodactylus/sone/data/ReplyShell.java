/*
 * Sone - ReplyShell.java - Copyright © 2010 David Roden
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
 * A shell around a {@link Reply} for replies that have not yet been retrieved
 * from Freenet.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ReplyShell extends Reply implements Shell<Reply> {

	/** The Sone that posted this reply. */
	private Sone sone;

	/** The ID of the reply. */
	private UUID id;

	/** The Post this reply refers to. */
	private Post post;

	/** The time of the reply. */
	private Long time;

	/** The text of the reply. */
	private String text;

	/**
	 * Creates a new reply shell.
	 */
	public ReplyShell() {
		super(null, null, null);
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the Sone that posted this reply.
	 *
	 * @return The Sone that posted this reply
	 */
	@Override
	public Sone getSone() {
		return sone;
	}

	/**
	 * Sets the Sone that posted this reply.
	 *
	 * @param sone
	 *            The sone that pasted this reply
	 * @return This reply shell (for method chaining)
	 */
	public ReplyShell setSone(Sone sone) {
		this.sone = sone;
		return this;
	}

	/**
	 * Returns the ID of the reply.
	 *
	 * @return The ID of the reply
	 */
	@Override
	public String getId() {
		return id.toString();
	}

	/**
	 * Sets the ID of this reply.
	 *
	 * @param id
	 *            The ID of this reply
	 * @return This reply shell (for method chaining)
	 */
	public ReplyShell setId(UUID id) {
		this.id = id;
		return this;
	}

	/**
	 * Returns the post this reply refers to.
	 *
	 * @return The post this reply refers to
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
	 * @return This reply shell (for method chaining)
	 */
	public ReplyShell setPost(Post post) {
		this.post = post;
		return this;
	}

	/**
	 * Returns the time of the reply.
	 *
	 * @return The time of the reply (in milliseconds since Jan 1, 1970 UTC)
	 */
	@Override
	public long getTime() {
		return time;
	}

	/**
	 * Sets the time of this reply.
	 *
	 * @param time
	 *            The time of this reply (in milliseconds since Jan 1, 1970 UTC)
	 * @return This reply shell (for method chaining)
	 */
	public ReplyShell setTime(long time) {
		this.time = time;
		return this;
	}

	/**
	 * Returns the text of the reply.
	 *
	 * @return The text of the reply
	 */
	@Override
	public String getText() {
		return text;
	}

	/**
	 * Sets the text of the reply.
	 *
	 * @param text
	 *            The text of the reply
	 * @return This reply shell (for method chaining)
	 */
	public ReplyShell setText(String text) {
		this.text = text;
		return this;
	}

	//
	// INTERFACE Shell
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canUnshell() {
		return (sone != null) && (id != null) && (post != null) && (time != null) && (text != null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Reply getShelled() {
		if (canUnshell()) {
			return new Reply(sone, id, post, time, text);
		}
		return this;
	}

}
