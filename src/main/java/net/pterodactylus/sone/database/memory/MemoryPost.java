/*
 * Sone - MemoryPost.java - Copyright © 2010–2016 David Roden
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

package net.pterodactylus.sone.database.memory;

import static com.google.common.base.Optional.fromNullable;

import java.util.UUID;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.database.SoneProvider;

import com.google.common.base.Optional;

/**
 * A post is a short message that a user writes in his Sone to let other users
 * know what is going on.
 */
class MemoryPost implements Post {

	/** The post database. */
	private final MemoryDatabase postDatabase;

	/** The Sone provider. */
	private final SoneProvider soneProvider;

	/** The GUID of the post. */
	private final UUID id;

	/** The ID of the owning Sone. */
	private final String soneId;

	/** The ID of the recipient Sone. */
	private final String recipientId;

	/** The time of the post (in milliseconds since Jan 1, 1970 UTC). */
	private final long time;

	/** The text of the post. */
	private final String text;

	/**
	 * Creates a new post.
	 *
	 * @param postDatabase
	 *            The post database
	 * @param soneProvider
	 *            The Sone provider
	 * @param id
	 *            The ID of the post
	 * @param soneId
	 *            The ID of the Sone this post belongs to
	 * @param recipientId
	 *            The ID of the recipient of the post
	 * @param time
	 *            The time of the post (in milliseconds since Jan 1, 1970 UTC)
	 * @param text
	 *            The text of the post
	 */
	public MemoryPost(MemoryDatabase postDatabase, SoneProvider soneProvider, String id, String soneId, String recipientId, long time, String text) {
		this.postDatabase = postDatabase;
		this.soneProvider = soneProvider;
		this.id = UUID.fromString(id);
		this.soneId = soneId;
		this.recipientId = recipientId;
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

	@Override
	public boolean isLoaded() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Sone getSone() {
		return soneProvider.getSone(soneId);
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public Optional<String> getRecipientId() {
		return fromNullable(recipientId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<Sone> getRecipient() {
		return recipientId == null ? Optional.<Sone>absent() : fromNullable(soneProvider.getSone(recipientId));
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
		return postDatabase.isPostKnown(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MemoryPost setKnown(boolean known) {
		postDatabase.setPostKnown(this, known);
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
		if (!(object instanceof MemoryPost)) {
			return false;
		}
		MemoryPost post = (MemoryPost) object;
		return post.id.equals(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return String.format("%s[id=%s,sone=%s,recipient=%s,time=%d,text=%s]", getClass().getName(), id, soneId, recipientId, time, text);
	}

}
