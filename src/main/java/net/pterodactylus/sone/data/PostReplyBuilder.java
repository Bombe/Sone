/*
 * Sone - PostReplyBuilder.java - Copyright © 2013 David Roden
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

/**
 * Builder for a {@link PostReply} object.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface PostReplyBuilder extends ReplyBuilder<PostReplyBuilder> {

	/**
	 * Configures this builder to set the given post as post the created reply
	 * refers to.
	 *
	 * @param postId
	 *            The ID of the post the reply refers to
	 * @return This builder
	 */
	public PostReplyBuilder to(String postId);

	/**
	 * Verifies the configuration of this builder and creates a new post reply.
	 * <p>
	 * The following conditions must be met in order for the configuration to be
	 * considered valid:
	 * <ul>
	 * <li>Exactly one of {@link #randomId()} or {@link #withId(String)} must
	 * have been called.</li>
	 * <li>The {@link #from(String) sender} must not be {@code null}.</li>
	 * <li>Exactly one of {@link #currentTime()} or {@link #withTime(long)} must
	 * have been called.</li>
	 * <li>The {@link #withText(String) text} must not be {@code null} and must
	 * contain something other than whitespace.</li>
	 * <li>The {@link #to(String) post} have been set.</li>
	 * </ul>
	 *
	 * @return The created post reply
	 * @throws IllegalStateException
	 *             if this builder’s configuration is not valid
	 */
	public PostReply build() throws IllegalStateException;

}
