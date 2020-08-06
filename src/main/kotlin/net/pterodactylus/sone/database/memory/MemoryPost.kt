/*
 * Sone - MemoryPost.kt - Copyright © 2010–2020 David Roden
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

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.database.PostBuilder
import net.pterodactylus.sone.database.SoneProvider
import net.pterodactylus.sone.utils.asOptional

/**
 * A post is a short message that a user writes in his Sone to let other users
 * know what is going on.
 */
class MemoryPost(
		private val postDatabase: MemoryDatabase,
		private val soneProvider: SoneProvider,
		override val id: String,
		private val soneId: String,
		private val recipientId: String?,
		private val time: Long,
		private val text: String
) : Post {

	override fun isLoaded() = true

	override fun getSone(): Sone = soneProvider.getSone(soneId)!!

	override fun getRecipientId() = recipientId.asOptional()

	override fun getRecipient() = recipientId?.let(soneProvider::getSone).asOptional()

	override fun getTime() = time

	override fun getText() = text

	override fun isKnown() = postDatabase.isPostKnown(this)

	override fun setKnown(known: Boolean) = apply {
		postDatabase.setPostKnown(this, known)
	}

	override fun hashCode() = id.hashCode()

	override fun equals(other: Any?) = (other is MemoryPost) && (other.id == id)

	override fun toString() = "${javaClass.name}[id=$id,sone=$soneId,recipient=$recipientId,time=$time,text=$text]"

	data class Shell(val id: String, val soneId: String, val recipientId: String?, val time: Long, val text: String) {

		fun build(postBuilder: PostBuilder) =
				postBuilder.withId(id).from(soneId).let { if (recipientId != null) it.to(recipientId) else it }.withTime(time).withText(text).build()

	}

}

fun Post.toShell() = MemoryPost.Shell(id, sone!!.id, recipient.orNull()?.id, time, text)
