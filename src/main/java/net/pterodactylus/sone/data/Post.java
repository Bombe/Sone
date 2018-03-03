/*
 * Sone - Post.java - Copyright © 2010–2016 David Roden
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

import static com.google.common.base.Optional.absent;

import java.util.Comparator;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

/**
 * A post is a short message that a user writes in his Sone to let other users
 * know what is going on.
 */
public interface Post extends Identified {

	/** Comparator for posts, sorts descending by time. */
	public static final Comparator<Post> NEWEST_FIRST = new Comparator<Post>() {

		@Override
		public int compare(Post leftPost, Post rightPost) {
			return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, rightPost.getTime() - leftPost.getTime()));
		}

	};

	/** Filter for posts with timestamps from the future. */
	public static final Predicate<Post> FUTURE_POSTS_FILTER = new Predicate<Post>() {

		@Override
		public boolean apply(Post post) {
			return (post != null) && (post.getTime() <= System.currentTimeMillis());
		}

	};

	//
	// ACCESSORS
	//

	/**
	 * Returns the ID of the post.
	 *
	 * @return The ID of the post
	 */
	public String getId();

	/**
	 * Returns whether this post has already been loaded.
	 *
	 * @return {@code true} if this post has already been loaded, {@code
	 * false} otherwise
	 */
	boolean isLoaded();

	/**
	 * Returns the Sone this post belongs to.
	 *
	 * @return The Sone of this post
	 */
	public Sone getSone();

	/**
	 * Returns the ID of the recipient {@link Sone}, or
	 * {@link Optional#absent()} if this post does not have a recipient.
	 *
	 * @return The ID of the recipient, or {@link Optional#absent()}
	 */
	public Optional<String> getRecipientId();

	/**
	 * Returns the recipient of this post, if any.
	 *
	 * @return The recipient of this post, or {@link Optional#absent()} if there
	 *         is no recipient
	 */
	public Optional<Sone> getRecipient();

	/**
	 * Returns the time of the post.
	 *
	 * @return The time of the post (in milliseconds since Jan 1, 1970 UTC)
	 */
	public long getTime();

	/**
	 * Returns the text of the post.
	 *
	 * @return The text of the post
	 */
	public String getText();

	/**
	 * Returns whether this post is known.
	 *
	 * @return {@code true} if this post is known, {@code false} otherwise
	 */
	public boolean isKnown();

	/**
	 * Sets whether this post is known.
	 *
	 * @param known
	 *            {@code true} if this post is known, {@code false} otherwise
	 * @return This post
	 */
	public Post setKnown(boolean known);

	/**
	 * Shell for a post that has not yet been loaded.
	 */
	public static class EmptyPost implements Post {

		private final String id;

		public EmptyPost(String id) {
			this.id = id;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public boolean isLoaded() {
			return false;
		}

		@Override
		public Sone getSone() {
			return null;
		}

		@Override
		public Optional<String> getRecipientId() {
			return absent();
		}

		@Override
		public Optional<Sone> getRecipient() {
			return absent();
		}

		@Override
		public long getTime() {
			return 0;
		}

		@Override
		public String getText() {
			return null;
		}

		@Override
		public boolean isKnown() {
			return false;
		}

		@Override
		public Post setKnown(boolean known) {
			return this;
		}

	}

}
