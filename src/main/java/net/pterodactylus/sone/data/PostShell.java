/*
 * Sone - PostShell.java - Copyright © 2010 David Roden
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.util.logging.Logging;

/**
 * {@link Shell} around a {@link Post} that has not yet been retrieved from
 * Freenet.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PostShell extends Post implements Shell<Post> {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(PostShell.class);

	/** The shell creator. */
	public static final ShellCreator<Post> creator = new ShellCreator<Post>() {

		@Override
		public Shell<Post> createShell(String id) {
			return new PostShell().setId(id);
		}
	};

	/** The GUID of the post. */
	private UUID id;

	/** The Sone this post belongs to. */
	private Sone sone;

	/** The time of the post (in milliseconds since Jan 1, 1970 UTC). */
	private Long time;

	/** The text of the post. */
	private String text;

	/** The replies that have been loaded for this post. */
	private final Set<Reply> replies = new HashSet<Reply>();

	/**
	 * Creates a new post shell.
	 */
	public PostShell() {
		super(null, null);
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the ID of the post.
	 *
	 * @return The ID of the post
	 */
	@Override
	public String getId() {
		return id.toString();
	}

	/**
	 * Sets the ID of the post.
	 *
	 * @param id
	 *            The ID of the post
	 * @return This post shell (for method chaining)
	 */
	public PostShell setId(String id) {
		try {
			this.id = UUID.fromString(id);
		} catch (IllegalArgumentException iae1) {
			logger.log(Level.WARNING, "Invalid ID: “" + id + "”.", iae1);
			this.id = UUID.randomUUID();
		}
		return this;
	}

	/**
	 * Returns the Sone this post belongs to.
	 *
	 * @return The Sone of this post
	 */
	@Override
	public Sone getSone() {
		return sone;
	}

	/**
	 * Sets the Sone the post belongs to.
	 *
	 * @param sone
	 *            The Sone the post belongs to
	 * @return This post shell (for method chaining)
	 */
	public PostShell setSone(Sone sone) {
		this.sone = sone;
		return this;
	}

	/**
	 * Returns the time of the post.
	 *
	 * @return The time of the post (in milliseconds since Jan 1, 1970 UTC)
	 */
	@Override
	public long getTime() {
		return time;
	}

	/**
	 * Sets the time of the post.
	 *
	 * @param time
	 *            The time of the post (in milliseconds since Jan 1, 1970 UTC)
	 * @return This post shell (for method chaining)
	 */
	public PostShell setTime(long time) {
		this.time = time;
		return this;
	}

	/**
	 * Returns the text of the post.
	 *
	 * @return The text of the post
	 */
	@Override
	public String getText() {
		return text;
	}

	/**
	 * Sets the text of the post.
	 *
	 * @param text
	 *            The text of the post.
	 * @return This post shell (for method chaining)
	 */
	public PostShell setText(String text) {
		this.text = text;
		return this;
	}

	/**
	 * Returns all replies to this post in unspecified order.
	 *
	 * @return All replies to this post
	 */
	@Override
	public Set<Reply> getReplies() {
		return Collections.unmodifiableSet(replies);
	}

	/**
	 * Adds a reply to this post. The reply will not be added if its
	 * {@link Reply#getPost() post} is not equal to this post.
	 *
	 * @param reply
	 *            The reply to add
	 */
	@Override
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
	@Override
	public void removeReply(Reply reply) {
		if (reply.getPost().equals(this)) {
			replies.remove(reply);
		}
	}

	//
	// INTERFACE Shell
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canUnshell() {
		return (id != null) && (sone != null) && (!(sone instanceof Shell<?>)) && (time != null) && (text != null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Post getShelled() {
		if (canUnshell()) {
			Post post = new Post(id, sone, time, text);
			for (Reply reply : replies) {
				post.addReply(reply);
			}
		}
		return this;
	}

}
