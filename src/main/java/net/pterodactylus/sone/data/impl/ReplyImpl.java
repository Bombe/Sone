/*
 * Sone - ReplyImpl.java - Copyright © 2011–2012 David Roden
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

import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;

/**
 * Abstract base class for all replies.
 *
 * @param <T>
 *            The type of the reply
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class ReplyImpl<T extends Reply<T>> implements Reply<T> {

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
	protected ReplyImpl(String id) {
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
	protected ReplyImpl(Sone sone, long time, String text) {
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
	protected ReplyImpl(String id, Sone sone, long time, String text) {
		this.id = id;
		this.sone = sone;
		this.time = time;
		this.text = text;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	@Override
	@SuppressWarnings("unchecked")
	public T setSone(Sone sone) {
		this.sone = sone;
		return (T) this;
	}

	/**
	 * {@inheritDoc}
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
	 * @return This reply (for method chaining)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T setTime(long time) {
		this.time = time;
		return (T) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	@Override
	@SuppressWarnings("unchecked")
	public T setText(String text) {
		this.text = text;
		return (T) this;
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
		return reply.getId().equals(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getClass().getName() + "[id=" + id + ",sone=" + sone + ",time=" + time + ",text=" + text + "]";
	}

}
