/**
 * Sone - SoneMentionDetectorTest.kt - Copyright © 2019 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.text

import com.google.common.base.*
import com.google.common.base.Optional.*
import com.google.common.eventbus.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.database.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

/**
 * Unit test for [SoneMentionDetector].
 */
class SoneMentionDetectorTest {

	private val eventBus = EventBus()
	private val soneProvider = TestSoneProvider()
	private val postProvider = TestPostProvider()
	private val soneTextParser = SoneTextParser(soneProvider, postProvider)
	private val capturedEvents = mutableListOf<MentionOfLocalSoneFoundEvent>()

	init {
		eventBus.register(SoneMentionDetector(eventBus, soneTextParser))
		eventBus.register(object : Any() {
			@Subscribe
			fun captureEvent(mentionOfLocalSoneFoundEvent: MentionOfLocalSoneFoundEvent) {
				capturedEvents += mentionOfLocalSoneFoundEvent
			}
		})
	}

	@Test
	fun `detector does not emit event on post that does not contain any sones`() {
		val post = createPost()
		eventBus.post(NewPostFoundEvent(post))
		assertThat(capturedEvents, emptyIterable())
	}

	@Test
	fun `detector does not emit event on post that does contain two remote sones`() {
		val post = createPost("text mentions sone://${remoteSone1.id} and sone://${remoteSone2.id}.")
		eventBus.post(NewPostFoundEvent(post))
		assertThat(capturedEvents, emptyIterable())
	}

	@Test
	fun `detector emits event on post that contains links to a remote and a local sone`() {
		val post = createPost("text mentions sone://${localSone1.id} and sone://${remoteSone2.id}.")
		eventBus.post(NewPostFoundEvent(post))
		assertThat(capturedEvents, contains(MentionOfLocalSoneFoundEvent(post)))
	}

	@Test
	fun `detector emits one event on post that contains two links to the same local sone`() {
		val post = createPost("text mentions sone://${localSone1.id} and sone://${localSone1.id}.")
		eventBus.post(NewPostFoundEvent(post))
		assertThat(capturedEvents, contains(MentionOfLocalSoneFoundEvent(post)))
	}

	@Test
	fun `detector emits one event on post that contains links to two local sones`() {
		val post = createPost("text mentions sone://${localSone1.id} and sone://${localSone2.id}.")
		eventBus.post(NewPostFoundEvent(post))
		assertThat(capturedEvents, contains(MentionOfLocalSoneFoundEvent(post)))
	}

	@Test
	fun `detector does not emit event for post by local sone`() {
		val post = createPost("text mentions sone://${localSone1.id} and sone://${localSone2.id}.", localSone1)
		eventBus.post(NewPostFoundEvent(post))
		assertThat(capturedEvents, emptyIterable())
	}

	@Test
	fun `detector does not emit event for reply that contains no sones`() {
		val reply = emptyPostReply()
		eventBus.post(NewPostReplyFoundEvent(reply))
		assertThat(capturedEvents, emptyIterable())
	}

	@Test
	fun `detector does not emit event for reply that contains two links to remote sones`() {
		val reply = emptyPostReply("text mentions sone://${remoteSone1.id} and sone://${remoteSone2.id}.")
		eventBus.post(NewPostReplyFoundEvent(reply))
		assertThat(capturedEvents, emptyIterable())
	}

	@Test
	fun `detector emits event on reply that contains links to a remote and a local sone`() {
		val post = createPost()
		val reply = emptyPostReply("text mentions sone://${remoteSone1.id} and sone://${localSone1.id}.", post)
		eventBus.post(NewPostReplyFoundEvent(reply))
		assertThat(capturedEvents, contains(MentionOfLocalSoneFoundEvent(post)))
	}

	@Test
	fun `detector emits one event on reply that contains two links to the same local sone`() {
		val post = createPost()
		val reply = emptyPostReply("text mentions sone://${localSone1.id} and sone://${localSone1.id}.", post)
		eventBus.post(NewPostReplyFoundEvent(reply))
		assertThat(capturedEvents, contains(MentionOfLocalSoneFoundEvent(post)))
	}

	@Test
	fun `detector emits one event on reply that contains two links to local sones`() {
		val post = createPost()
		val reply = emptyPostReply("text mentions sone://${localSone1.id} and sone://${localSone2.id}.", post)
		eventBus.post(NewPostReplyFoundEvent(reply))
		assertThat(capturedEvents, contains(MentionOfLocalSoneFoundEvent(post)))
	}

	@Test
	fun `detector does not emit event for reply by local sone`() {
		val reply = emptyPostReply("text mentions sone://${localSone1.id} and sone://${localSone2.id}.", sone = localSone1)
		eventBus.post(NewPostReplyFoundEvent(reply))
		assertThat(capturedEvents, emptyIterable())
	}

}

private val remoteSone1 = createRemoteSone()
private val remoteSone2 = createRemoteSone()

private val localSone1 = createLocalSone()
private val localSone2 = createLocalSone()

private fun createPost(text: String = "", sone: Sone = remoteSone1): Post.EmptyPost {
	return object : Post.EmptyPost("post-id") {
		override fun getSone() = sone
		override fun getText() = text
	}
}

private class TestSoneProvider : SoneProvider {

	override val sones: Collection<Sone> get() = remoteSones + localSones
	override val localSones: Collection<Sone> get() = setOf(localSone1, localSone2)
	override val remoteSones: Collection<Sone> get() = setOf(remoteSone1, remoteSone2)
	override val soneLoader: (String) -> Sone? get() = this::getSone
	override fun getSone(soneId: String): Sone? =
			localSones.firstOrNull { it.id == soneId } ?: remoteSones.firstOrNull { it.id == soneId }

}

private class TestPostProvider : PostProvider {

	override fun getPost(postId: String): Post? = null
	override fun getPosts(soneId: String): Collection<Post> = emptyList()
	override fun getDirectedPosts(recipientId: String): Collection<Post> = emptyList()

}

private fun emptyPostReply(text: String = "", post: Post = createPost(), sone: Sone = remoteSone1) = object : PostReply {
	override val id = "reply-id"
	override fun getSone() = sone
	override fun getPostId() = post.id
	override fun getPost(): Optional<Post> = of(post)
	override fun getTime() = 1L
	override fun getText() = text
	override fun isKnown() = false
	override fun setKnown(known: Boolean): PostReply = this
}
