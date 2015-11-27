/*
 * Sone - AbstractReplyBuilder.java - Copyright © 2013–2015 David Roden
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

import net.pterodactylus.sone.database.ReplyBuilder;

/**
 * Abstract implementation of a {@link ReplyBuilder}.
 *
 * @param <B>
 *            The interface implemented and exposed by the builder
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class AbstractReplyBuilder<B extends ReplyBuilder<B>> implements ReplyBuilder<B> {

	/** Whether to use a random ID for the reply. */
	protected boolean randomId;

	/** The ID of the reply. */
	protected String id;

	/** The sender of the reply. */
	protected String senderId;

	/** Whether to use the current time when creating the reply. */
	protected boolean currentTime;

	/** The time of the reply. */
	protected long time;

	/** The text of the reply. */
	protected String text;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public B randomId() {
		this.randomId = true;
		return (B) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public B withId(String id) {
		this.id = id;
		return (B) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public B from(String senderId) {
		this.senderId = senderId;
		return (B) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public B currentTime() {
		this.currentTime = true;
		return (B) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public B withTime(long time) {
		this.time = time;
		return (B) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public B withText(String text) {
		this.text = text;
		return (B) this;
	}

}
