/*
 * Sone - Reply.java - Copyright © 2011 David Roden
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

import java.util.Comparator;
import java.util.UUID;

import net.pterodactylus.util.filter.Filter;

/**
 * Abstract base class for all replies.
 *
 * @param <T>
 *            The type of the reply
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class Reply<T extends Reply<T>> {

	/** Comparator that sorts replies ascending by time. */
	public static final Comparator<Reply<?>> TIME_COMPARATOR = new Comparator<Reply<?>>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(Reply<?> leftReply, Reply<?> rightReply) {
			return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, leftReply.getTime() - rightReply.getTime()));
		}

	};

	/** Filter for replies with timestamps from the future. */
	public static final Filter<Reply<?>> FUTURE_REPLY_FILTER = new Filter<Reply<?>>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean filterObject(Reply<?> reply) {
			return reply.getTime() <= System.currentTimeMillis();
		}

	};

	/** The ID of the reply. */
	private final String id;

	/** The Sone that created this reply. */
	private volatile Sone sone;

	/** The time of the reply. */
	private volatile long time;

	/** The text of the reply. */
	private volatile String text;

	/** Whether the reply is known. */
	private volatile boolean known;

	/**
	 * Creates a new reply with the given ID.
	 *
	 * @param id
	 *            The ID of the reply
	 */
	protected Reply(String id) {
		this(id, null, 0, null);
	}

	/**
	 * Creates a new reply with a new random ID.
	 *
	 * @param sone
	 *            The Sone of the reply
	 * @param time
	 *            The time of the reply
	 * @param text
	 *            The text of the reply
	 */
	protected Reply(Sone sone, long time, String text) {
		this(UUID.randomUUID().toString(), sone, time, text);
	}

	/**
	 * Creates a new reply.
	 *
	 * @param id
	 *            The ID of the reply
	 * @param sone
	 *            The Sone of the reply
	 * @param time
	 *            The time of the reply
	 * @param text
	 *            The text of the reply
	 */
	protected Reply(String id, Sone sone, long time, String text) {
		this.id = id;
		this.sone = sone;
		this.time = time;
		this.text = text;
	}

	/**
	 * Returns the ID of the reply.
	 *
	 * @return The ID of the reply
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the Sone that posted this reply.
	 *
	 * @return The Sone that posted this reply
	 */
	public Sone getSone() {
		return sone;
	}

	/**
	 * Sets the Sone that posted this reply.
	 *
	 * @param sone
	 *            The Sone that posted this reply
	 * @return This reply (for method chaining)
	 */
	@SuppressWarnings("unchecked")
	public T setSone(Sone sone) {
		this.sone = sone;
		return (T) this;
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
	 * Sets the time of this reply.
	 *
	 * @param time
	 *            The time of this reply (in milliseconds since Jan 1, 1970 UTC)
	 * @return This reply (for method chaining)
	 */
	@SuppressWarnings("unchecked")
	public T setTime(long time) {
		this.time = time;
		return (T) this;
	}

	/**
	 * Returns the text of the reply.
	 *
	 * @return The text of the reply
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text of this reply.
	 *
	 * @param text
	 *            The text of this reply
	 * @return This reply (for method chaining)
	 */
	@SuppressWarnings("unchecked")
	public T setText(String text) {
		this.text = text;
		return (T) this;
	}

	/**
	 * Returns whether this reply is known.
	 *
	 * @return {@code true} if this reply is known, {@code false} otherwise
	 */
	public boolean isKnown() {
		return known;
	}

	/**
	 * Sets whether this reply is known.
	 *
	 * @param known
	 *            {@code true} if this reply is known, {@code false} otherwise
	 * @return This reply
	 */
	@SuppressWarnings("unchecked")
	public T setKnown(boolean known) {
		this.known = known;
		return (T) this;
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
		if (!(object instanceof Reply<?>)) {
			return false;
		}
		Reply<?> reply = (Reply<?>) object;
		return reply.id.equals(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getClass().getName() + "[id=" + id + ",sone=" + sone + ",time=" + time + ",text=" + text + "]";
	}

}
