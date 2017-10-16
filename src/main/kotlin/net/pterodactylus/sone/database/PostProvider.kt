/*
 * Sone - PostProvider.java - Copyright © 2011–2016 David Roden
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

package net.pterodactylus.sone.database

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.database.memory.MemoryDatabase

import com.google.common.base.Optional
import com.google.inject.ImplementedBy

/**
 * Interface for objects that can provide [Post]s by their ID.
 */
@ImplementedBy(MemoryDatabase::class)
interface PostProvider {

	fun getPost(postId: String): Post?
	fun getPosts(soneId: String): Collection<Post>
	fun getDirectedPosts(recipientId: String): Collection<Post>

}
