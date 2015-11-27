/*
 * Sone - PostProvider.java - Copyright © 2011–2015 David Roden
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

import java.util.Collection;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.database.memory.MemoryDatabase;

import com.google.common.base.Optional;
import com.google.inject.ImplementedBy;

/**
 * Interface for objects that can provide {@link Post}s by their ID.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
@ImplementedBy(MemoryDatabase.class)
public interface PostProvider {

	/**
	 * Returns the post with the given ID.
	 *
	 * @param postId
	 *            The ID of the post to return
	 * @return The post with the given ID, or {@code null}
	 */
	public Optional<Post> getPost(String postId);

	/**
	 * Returns all posts from the given Sone.
	 *
	 * @param soneId
	 *            The ID of the Sone
	 * @return All posts from the given Sone
	 */
	public Collection<Post> getPosts(String soneId);

	/**
	 * Returns all posts that have the given Sone as recipient.
	 *
	 * @see Post#getRecipient()
	 * @param recipientId
	 *            The ID of the recipient of the posts
	 * @return All posts that have the given Sone as recipient
	 */
	public Collection<Post> getDirectedPosts(String recipientId);

}
