/*
 * Sone - PostReply.java - Copyright © 2010–2015 David Roden
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

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

/**
 * A reply is like a {@link Post} but can never be posted on its own, it always
 * refers to another {@link Post}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface PostReply extends Reply<PostReply> {

	/**
	 * Filter that selects {@link PostReply}s that have a
	 * {@link Optional#isPresent() present} {@link #getPost() post}.
	 */
	public static final Predicate<PostReply> HAS_POST_FILTER = new Predicate<PostReply>() {

		@Override
		public boolean apply(PostReply postReply) {
			return (postReply != null) && postReply.getPost().isPresent();
		}
	};

	/**
	 * Returns the ID of the post this reply refers to.
	 *
	 * @return The ID of the post this reply refers to
	 */
	public String getPostId();

	/**
	 * Returns the post this reply refers to.
	 *
	 * @return The post this reply refers to
	 */
	public Optional<Post> getPost();

}
