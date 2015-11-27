/*
 * Sone - ReplyBuilder.java - Copyright © 2013–2015 David Roden
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

package net.pterodactylus.sone.database;

import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;

/**
 * Methods that all reply builders need to implement in order to be able to
 * create any kind of {@link Reply}.
 *
 * @param <B>
 *            The type of the builder
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface ReplyBuilder<B extends ReplyBuilder<B>> {

	/**
	 * Configures this builder to use a random ID when creating the reply. If
	 * this method is used, {@link #withId(String)} must not be used.
	 *
	 * @return This builder
	 */
	public B randomId();

	/**
	 * Configures this builder to use the given ID when creating the reply. If
	 * this method is used, {@link #randomId()} must not be used.
	 *
	 * @param id
	 *            The ID of the reply
	 * @return This builder
	 */
	public B withId(String id);

	/**
	 * Configures this builder to use the ID of the given {@link Sone} as sender
	 * of the reply.
	 *
	 * @param senderId
	 *            The ID of the sender of the reply
	 * @return This builder
	 */
	public B from(String senderId);

	/**
	 * Configures this builder to use the current time when creating the reply.
	 * If this method is used, {@link #withTime(long)} must not be used.
	 *
	 * @return This builder
	 */
	public B currentTime();

	/**
	 * Configures this builder to use the given time when creating the reply. If
	 * this method is used, {@link #currentTime()} must not be used.
	 *
	 * @param time
	 *            The time of the reply
	 * @return This builder
	 */
	public B withTime(long time);

	/**
	 * Configures this builder to use the given text when creating the reply.
	 *
	 * @param text
	 *            The text of the reply
	 * @return This builder
	 */
	public B withText(String text);

}
