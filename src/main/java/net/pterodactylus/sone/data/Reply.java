/*
 * Sone - Reply.java - Copyright © 2010–2020 David Roden
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

import com.google.common.base.Predicate;

/**
 * Defines methods common for all replies.
 *
 * @param <T>
 *            The type of the reply
 */
public interface Reply<T extends Reply<T>> extends Identified {

	/** Filter for replies with timestamps from the future. */
	public static final Predicate<Reply<?>> FUTURE_REPLY_FILTER = new Predicate<Reply<?>>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean apply(Reply<?> reply) {
			return (reply != null) && (reply.getTime() <= System.currentTimeMillis());
		}

	};

	/**
	 * Returns the ID of the reply.
	 *
	 * @return The ID of the reply
	 */
	public String getId();

	/**
	 * Returns the Sone that posted this reply.
	 *
	 * @return The Sone that posted this reply
	 */
	public Sone getSone();

	/**
	 * Returns the time of the reply.
	 *
	 * @return The time of the reply (in milliseconds since Jan 1, 1970 UTC)
	 */
	public long getTime();

	/**
	 * Returns the text of the reply.
	 *
	 * @return The text of the reply
	 */
	public String getText();

	/**
	 * Returns whether this reply is known.
	 *
	 * @return {@code true} if this reply is known, {@code false} otherwise
	 */
	public boolean isKnown();

	/**
	 * Sets whether this reply is known.
	 *
	 * @param known
	 *            {@code true} if this reply is known, {@code false} otherwise
	 * @return This reply
	 */
	public T setKnown(boolean known);

}
