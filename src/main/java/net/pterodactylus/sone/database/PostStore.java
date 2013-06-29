/*
 * Sone - PostStore.java - Copyright © 2013 David Roden
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
import net.pterodactylus.sone.data.Sone;

/**
 * Interface for a store for posts.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface PostStore {

	/**
	 * Adds the given post to the store.
	 *
	 * @param post
	 *            The post to store
	 */
	public void storePost(Post post);

	/**
	 * Removes the given post.
	 *
	 * @param post
	 *            The post to remove
	 */
	public void removePost(Post post);

	/**
	 * Stores the given posts as all posts of a single {@link Sone}. This method
	 * will removed all other posts from the Sone!
	 *
	 * @param sone
	 *            The Sone to store the posts for
	 * @param posts
	 *            The posts to store
	 * @throws IllegalArgumentException
	 *             if posts do not all belong to the same Sone
	 */
	public void storePosts(Sone sone, Collection<Post> posts) throws IllegalArgumentException;

	/**
	 * Removes all posts of the given {@link Sone}
	 *
	 * @param sone
	 *            The Sone to remove all posts for
	 */
	public void removePosts(Sone sone);

}
