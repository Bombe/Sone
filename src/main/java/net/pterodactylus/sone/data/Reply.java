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

	/**
	 * Returns the ID of the reply.
	 *
	 * @return The ID of the reply
	 */
	public abstract String getId();

	/**
	 * Returns the Sone that posted this reply.
	 *
	 * @return The Sone that posted this reply
	 */
	public abstract Sone getSone();

	/**
	 * Returns the time of the reply.
	 *
	 * @return The time of the reply (in milliseconds since Jan 1, 1970 UTC)
	 */
	public abstract long getTime();

	/**
	 * Returns the text of the reply.
	 *
	 * @return The text of the reply
	 */
	public abstract String getText();

}
