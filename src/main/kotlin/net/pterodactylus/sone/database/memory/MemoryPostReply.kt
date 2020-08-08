/*
 * Sone - MemoryPostReply.kt - Copyright © 2013–2020 David Roden
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
package net.pterodactylus.sone.database.memory

import com.google.common.base.Optional
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.PostReplyShell
import net.pterodactylus.sone.database.PostReplyBuilder
import net.pterodactylus.sone.database.SoneProvider
import net.pterodactylus.sone.utils.asOptional

/**
 * Memory-based [PostReply] implementation.
 */
class MemoryPostReply(
		private val database: MemoryDatabase,
		private val soneProvider: SoneProvider,
		override val id: String,
		private val soneId: String,
		private val time: Long,
		private val text: String,
		private val postId: String) : PostReply {

	override fun getSone() = soneProvider.getSone(soneId)!!

	override fun getTime() = time

	override fun getText() = text

	override fun isKnown() = database.isPostReplyKnown(this)

	override fun getPostId() = postId

	override fun getPost(): Optional<Post> = database.getPost(postId).asOptional()

	override fun hashCode() = id.hashCode()

	override fun equals(other: Any?): Boolean {
		if (other !is MemoryPostReply) {
			return false
		}
		return other.id == id
	}

	override fun toString(): String {
		return "MemoryPostReply{" +
				"database=" + database +
				", soneProvider=" + soneProvider +
				", id='" + id + '\'' +
				", soneId='" + soneId + '\'' +
				", time=" + time +
				", text='" + text + '\'' +
				", postId='" + postId + '\'' +
				'}'
	}

}
