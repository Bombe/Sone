/*
 * Sone - PostImpl.java - Copyright © 2010–2013 David Roden
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
import net.pterodactylus.sone.data.Sone;

/**
 * A post is a short message that a user writes in his Sone to let other users
 * know what is going on.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PostImpl implements Post {

	/** The GUID of the post. */
	private final UUID id;

	/** The Sone this post belongs to. */
	private final Sone sone;

	/** The Sone of the recipient. */
	private final Sone recipient;

	/** The time of the post (in milliseconds since Jan 1, 1970 UTC). */
	private final long time;

	/** The text of the post. */
	private final String text;

	/** Whether the post is known. */
	private volatile boolean known;

	/**
	 * Creates a new post.
	 *
	 * @param id
	 *            The ID of the post
	 * @param sone
	 *            The Sone this post belongs to
	 * @param recipient
	 *            The recipient of the post
	 * @param time
	 *            The time of the post (in milliseconds since Jan 1, 1970 UTC)
	 * @param text
	 *            The text of the post
	 */
	public PostImpl(String id, Sone sone, Sone recipient, long time, String text) {
		this.id = UUID.fromString(id);
		this.sone = sone;
		this.recipient = recipient;
		this.time = time;
		this.text = text;
	}

	//
	// ACCESSORS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return id.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Sone getSone() {
		return sone;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Sone getRecipient() {
		return recipient;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getTime() {
		return time;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getText() {
		return text;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isKnown() {
		return known;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PostImpl setKnown(boolean known) {
		this.known = known;
		return this;
	}

	//
	// OBJECT METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof PostImpl)) {
			return false;
		}
		PostImpl post = (PostImpl) object;
		return post.id.equals(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getClass().getName() + "[id=" + id + ",sone=" + sone + ",time=" + time + ",text=" + text + "]";
	}

}
