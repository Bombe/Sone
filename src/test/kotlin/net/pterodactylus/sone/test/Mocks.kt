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

import com.google.common.base.Optional
import freenet.crypt.DummyRandomSource
import freenet.keys.FreenetURI
import freenet.keys.InsertableClientSSK
import net.pterodactylus.sone.data.Image
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.SoneOptions.DefaultSoneOptions
import net.pterodactylus.sone.data.impl.IdOnlySone
import net.pterodactylus.sone.data.impl.ImageImpl
import net.pterodactylus.sone.freenet.wot.DefaultIdentity
import net.pterodactylus.sone.freenet.wot.DefaultOwnIdentity
import net.pterodactylus.sone.freenet.wot.Identity
import net.pterodactylus.sone.freenet.wot.OwnIdentity
import net.pterodactylus.sone.utils.asFreenetBase64
import net.pterodactylus.sone.utils.asOptional

val remoteSone1 = createRemoteSone()
val remoteSone2 = createRemoteSone()

val localSone1 = createLocalSone()
val localSone2 = createLocalSone()

val createRequestUri: FreenetURI get() = InsertableClientSSK.createRandom(DummyRandomSource(), "").uri
val createInsertUri: FreenetURI get() = InsertableClientSSK.createRandom(DummyRandomSource(), "").insertURI
fun createId() = InsertableClientSSK.createRandom(DummyRandomSource(), "").uri.routingKey.asFreenetBase64

fun createOwnIdentity(id: String = "", nickname: String = "", requestUri: String = "", insertUri: String = "", contexts: Set<String> = setOf(), properties: Map<String, String> = mapOf()): OwnIdentity =
		DefaultOwnIdentity(id, nickname, requestUri, insertUri).apply {
			setContexts(contexts)
			this.properties = properties
		}

fun createIdentity(id: String = "", nickname: String = "", requestUri: String = "", contexts: Set<String> = setOf(), properties: Map<String, String> = mapOf()): Identity =
		DefaultIdentity(id, nickname, requestUri).apply {
			setContexts(contexts)
			this.properties = properties
		}

fun createLocalSone(id: String = createId(), identity: Identity = createOwnIdentity(id)): Sone = object : IdOnlySone(id) {
	private val options = DefaultSoneOptions()
	private val friends = mutableListOf<String>()
	override fun getIdentity(): Identity = identity
	override fun getOptions() = options
	override fun isLocal() = true
	override fun getFriends() = friends
	override fun hasFriend(friendSoneId: String) = friendSoneId in friends
}

fun createRemoteSone(id: String = createId(), identity: Identity = createIdentity(id)): Sone = object : IdOnlySone(id) {
	override fun getIdentity(): Identity = identity
}

fun createPost(text: String = "", sone: Sone? = remoteSone1, known: Boolean = false, time: Long = 1, loaded: Boolean = true, recipient: Sone? = null): Post {
	return object : Post.EmptyPost("post-id") {
		override fun getRecipientId() = recipient?.id.asOptional()
		override fun getRecipient() = recipient.asOptional()
		override fun getSone() = sone
		override fun getText() = text
		override fun isKnown() = known
		override fun getTime() = time
		override fun isLoaded() = loaded
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
