/*
 * Sone - ReplyImpl.java - Copyright © 2011–2013 David Roden
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

import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.database.SoneProvider;

/**
 * Abstract base class for all replies.
 *
 * @param <T>
 *            The type of the reply
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class ReplyImpl<T extends Reply<T>> implements Reply<T> {

	/** The Sone provider. */
	private final SoneProvider soneProvider;

	/** The ID of the reply. */
	private final String id;

	/** The Sone that created this reply. */
	private final String soneId;

	/** The time of the reply. */
	private final long time;

	/** The text of the reply. */
	private final String text;

	/** Whether the reply is known. */
	private volatile boolean known;

	/**
	 * Creates a new reply.
	 *
	 * @param soneProvider
	 *            The Sone provider
	 * @param id
	 *            The ID of the reply
	 * @param soneId
	 *            The ID of the Sone of the reply
	 * @param time
	 *            The time of the reply
	 * @param text
	 *            The text of the reply
	 */
	protected ReplyImpl(SoneProvider soneProvider, String id, String soneId, long time, String text) {
		this.soneProvider = soneProvider;
		this.id = id;
		this.soneId = soneId;
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
		return soneProvider.getSone(soneId);
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
		return String.format("%s[id=%s,sone=%s,time=%d,text=%s]", getClass().getName(), id, soneId, time, text);
	}

}
