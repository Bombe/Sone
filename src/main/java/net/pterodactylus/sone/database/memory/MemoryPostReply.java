/*
 * Sone - MemoryPostReply.java - Copyright © 2013–2020 David Roden
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

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.database.SoneProvider;

import com.google.common.base.Optional;

/**
 * Memory-based {@link PostReply} implementation.
 */
class MemoryPostReply implements PostReply {

	/** The database. */
	private final MemoryDatabase database;

	/** The Sone provider. */
	private final SoneProvider soneProvider;

	/** The ID of the post reply. */
	private final String id;

	/** The ID of the owning Sone. */
	private final String soneId;

	/** The time of the post reply. */
	private final long time;

	/** The text of the post reply. */
	private final String text;

	/** The ID of the post this post reply refers to. */
	private final String postId;

	/**
	 * Creates a new memory-based {@link PostReply} implementation.
	 *
	 * @param database
	 *            The database
	 * @param soneProvider
	 *            The Sone provider
	 * @param id
	 *            The ID of the post reply
	 * @param soneId
	 *            The ID of the owning Sone
	 * @param time
	 *            The time of the post reply
	 * @param text
	 *            The text of the post reply
	 * @param postId
	 *            The ID of the post this post reply refers to
	 */
	public MemoryPostReply(MemoryDatabase database, SoneProvider soneProvider, String id, String soneId, long time, String text, String postId) {
		this.database = database;
		this.soneProvider = soneProvider;
		this.id = id;
		this.soneId = soneId;
		this.time = time;
		this.text = text;
		this.postId = postId;
	}

	//
	// REPLY METHODS
	//

	/**
	 * {@inheritDocs}
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public Sone getSone() {
		return soneProvider.getSone(soneId);
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public long getTime() {
		return time;
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public String getText() {
		return text;
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public boolean isKnown() {
		return database.isPostReplyKnown(this);
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public PostReply setKnown(boolean known) {
		database.setPostReplyKnown(this, known);
		return this;
	}

	//
	// POSTREPLY METHODS
	//

	/**
	 * {@inheritDocs}
	 */
	@Override
	public String getPostId() {
		return postId;
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public Optional<Post> getPost() {
		return fromNullable(database.getPost(postId));
	}

	//
	// OBJECT METHODS
	//

	/**
	 * {@inheritDocs}
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof MemoryPostReply)) {
			return false;
		}
		MemoryPostReply memoryPostReply = (MemoryPostReply) object;
		return memoryPostReply.id.equals(id);
	}

	@Override
	public String toString() {
		return "MemoryPostReply{" +
				"database=" + database +
				", soneProvider=" + soneProvider +
				", id='" + id + '\'' +
				", soneId='" + soneId + '\'' +
				", time=" + time +
				", text='" + text + '\'' +
				", postId='" + postId + '\'' +
				'}';
	}

}
