/*
 * Sone - PostCache.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.core;

import java.util.HashMap;
import java.util.Map;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostShell;
import net.pterodactylus.sone.data.Shell;

/**
 * {@link Shell}-aware {@link Map} from post IDs to {@link Post}s that exchanges
 * an existing {@link Shell} against the real object once it’s available.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PostCache {

	/** The posts. */
	private final Map<String, Post> posts = new HashMap<String, Post>();

	/**
	 * Stores the given post in this cache. If the given post is not a
	 * {@link Shell}, it is stored. If it is a {@link Shell} it is only stored
	 * if there is no post stored for the ID of the given post. If the given
	 * {@code post} is a {@link Shell}, it needs to have its
	 * {@link PostShell#setId(java.util.UUID) ID} set!
	 *
	 * @param post
	 *            The post to store
	 */
	public void store(Post post) {
		if (!(post instanceof Shell<?>) || !posts.containsKey(post.getId())) {
			posts.put(post.getId(), post);
		}
	}

	/**
	 * Returns the post with the given ID.
	 *
	 * @param id
	 *            The ID of the post
	 * @return The post with the given ID, or {@code null} if there is no post
	 *         with the given ID
	 */
	public Post get(String id) {
		return posts.get(id);
	}

}
