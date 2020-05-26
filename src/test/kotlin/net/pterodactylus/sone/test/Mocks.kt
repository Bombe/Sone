/**
 * Sone - Mocks.kt - Copyright © 2019–2020 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.test

import com.google.common.base.*
import freenet.crypt.*
import freenet.keys.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.data.SoneOptions.*
import net.pterodactylus.sone.data.impl.*
import net.pterodactylus.sone.utils.*

val remoteSone1 = createRemoteSone()
val remoteSone2 = createRemoteSone()

val localSone1 = createLocalSone()
val localSone2 = createLocalSone()

val createRequestUri: FreenetURI get() = InsertableClientSSK.createRandom(DummyRandomSource(), "").uri
val createInsertUri: FreenetURI get() = InsertableClientSSK.createRandom(DummyRandomSource(), "").insertURI
fun createId() = InsertableClientSSK.createRandom(DummyRandomSource(), "").uri.routingKey.asFreenetBase64

fun createLocalSone(id: String? = createId()): Sone = object : IdOnlySone(id) {
	private val options = DefaultSoneOptions()
	override fun getOptions() = options
	override fun isLocal() = true
}

fun createRemoteSone(id: String? = createId()): Sone = IdOnlySone(id)

fun createPost(text: String = "", sone: Sone = remoteSone1, known: Boolean = false, time: Long = 1): Post {
	return object : Post.EmptyPost("post-id") {
		override fun getSone() = sone
		override fun getText() = text
		override fun isKnown() = known
		override fun getTime() = time
	}
}

fun emptyPostReply(text: String = "", post: Post? = createPost(), sone: Sone = remoteSone1, known: Boolean = false, time: Long = 1) = object : PostReply {
	override val id = "reply-id"
	override fun getSone() = sone
	override fun getPostId() = post!!.id
	override fun getPost(): Optional<Post> = Optional.fromNullable(post)
	override fun getTime() = time
	override fun getText() = text
	override fun isKnown() = known
}

fun createImage(sone: Sone): Image =
		ImageImpl().modify().setSone(sone).update()
