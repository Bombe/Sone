/*
 * Sone - PostBuilder.java - Copyright © 2013 David Roden
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
 * Builder for {@link Post} objects.
 * <p>
 * A {@link Post} consists of the following elements:
 * <ul>
 * <li>an ID,</li>
 * <li>a {@link Sone sender},</li>
 * <li>an optional {@link Sone recipient},</li>
 * <li>a time,</li>
 * <li>and a text.</li>
 * </ul>
 * Except for the recipient, all this elements have to be configured on this
 * builder. For the ID you have the possibility to configure either a random ID
 * (which should be used for new posts) or a custom ID you specify (for creating
 * an existing post). For the time you can use the current time (again, for
 * creating new posts) or the given time (for loading posts). It is an error to
 * specify both ways for either the ID or the time.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface PostBuilder {

	/**
	 * Copies all attributes of the given post to this post builder.
	 *
	 * @param post
	 *            The post whose attributes to copy into this builder
	 * @return This builder
	 * @throws NullPointerException
	 *             if {@code post} is {@code null}
	 */
	public PostBuilder copyPost(Post post) throws NullPointerException;

	/**
	 * Configures this builder to use the given Sone as sender of the new post.
	 *
	 * @param senderId
	 *            The ID of the sender of the post
	 * @return This post builder
	 */
	public PostBuilder from(String senderId);

	/**
	 * Configures this builder to use a random ID for the new post. If this
	 * method is used, {@link #withId(String)} must not be used.
	 *
	 * @return This post builder
	 */
	public PostBuilder randomId();

	/**
	 * Configures this builder to use the given ID as ID for the new post. If
	 * this method is used, {@link #randomId()} must not be used.
	 *
	 * @param id
	 *            The ID to use for the post
	 * @return This post builder
	 */
	public PostBuilder withId(String id);

	/**
	 * Configures this builder to use the current time when creating the post.
	 * If this method is used, {@link #withTime(long)} must not be used.
	 *
	 * @return This post builder
	 */
	public PostBuilder currentTime();

	/**
	 * Configures the builder to use the given time as time for the new post. If
	 * this method is used, {@link #currentTime()} must not be used.
	 *
	 * @param time
	 *            The time to use for the post
	 * @return This post builder
	 */
	public PostBuilder withTime(long time);

	/**
	 * Configures the builder to use the given text for the new post.
	 *
	 * @param text
	 *            The text to use for the post
	 * @return This post builder
	 */
	public PostBuilder withText(String text);

	/**
	 * Configures the builder to use the given {@link Sone} as recipient for the
	 * post.
	 *
	 * @param recipientId
	 *            The ID of the recipient of the post
	 * @return This post builder
	 */
	public PostBuilder to(String recipientId);

	/**
	 * Verifies this builder’s configuration and creates a new post.
	 * <p>
	 * The following conditions must be met in order for this builder to be
	 * configured correctly:
	 * <ul>
	 * <li>Exactly one of {@link #randomId()} or {@link #withId(String)} must
	 * have been called.</li>
	 * <li>The {@link #from(String) sender} must not be {@code null}.</li>
	 * <li>Exactly one of {@link #currentTime()} or {@link #withTime(long)} must
	 * have been called.</li>
	 * <li>The {@link #withText(String) text} must not be {@code null} and must
	 * contain something other than whitespace.</li>
	 * <li>The {@link #to(String) recipient} must either not have been set, or
	 * it must have been set to a {@link Sone} other than {@link #from(String)
	 * the sender}.</li>
	 * </ul>
	 *
	 * @return A new post
	 * @throws IllegalStateException
	 *             if this builder’s configuration is not valid
	 */
	public Post build() throws IllegalStateException;

}
