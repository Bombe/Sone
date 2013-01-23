/*
 * Sone - Post.java - Copyright © 2010–2013 David Roden
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
 * A post is a short message that a user writes in his Sone to let other users
 * know what is going on.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface Post {

	/** Comparator for posts, sorts descending by time. */
	public static final Comparator<Post> TIME_COMPARATOR = new Comparator<Post>() {

		@Override
		public int compare(Post leftPost, Post rightPost) {
			return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, rightPost.getTime() - leftPost.getTime()));
		}

	};

	/** Filter for posts with timestamps from the future. */
	public static final Predicate<Post> FUTURE_POSTS_FILTER = new Predicate<Post>() {

		@Override
		public boolean apply(Post post) {
			return post.getTime() <= System.currentTimeMillis();
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
	 * Returns the Sone this post belongs to.
	 *
	 * @return The Sone of this post
	 */
	public Sone getSone();

	/**
	 * Returns the recipient of this post, if any.
	 *
	 * @return The recipient of this post, or {@code null}
	 */
	public Sone getRecipient();

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

}
