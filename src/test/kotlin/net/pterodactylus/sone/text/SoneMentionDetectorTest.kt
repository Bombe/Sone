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
@Suppress("UnstableApiUsage")
class SoneMentionDetectorTest {

	private val caughtExceptions = mutableListOf<Throwable>()
	private val eventBus = EventBus { exception, _ -> caughtExceptions += exception }
	private val soneProvider = TestSoneProvider()
	private val postProvider = TestPostProvider()
	private val soneTextParser = SoneTextParser(soneProvider, postProvider)
	private val capturedFoundEvents = mutableListOf<MentionOfLocalSoneFoundEvent>()
	private val capturedRemovedEvents = mutableListOf<MentionOfLocalSoneRemovedEvent>()
	private val postReplyProvider = TestPostReplyProvider()

	init {
		eventBus.register(SoneMentionDetector(eventBus, soneTextParser, postReplyProvider))
		eventBus.register(object : Any() {
			@Subscribe
			fun captureFoundEvent(mentionOfLocalSoneFoundEvent: MentionOfLocalSoneFoundEvent) {
				capturedFoundEvents += mentionOfLocalSoneFoundEvent
			}

			@Subscribe
			fun captureRemovedEvent(event: MentionOfLocalSoneRemovedEvent) {
				capturedRemovedEvents += event
			}
		})
	}

	@Test
	fun `detector does not emit event on post that does not contain any sones`() {
		val post = createPost()
		eventBus.post(NewPostFoundEvent(post))
		assertThat(capturedFoundEvents, emptyIterable())
	}

	@Test
	fun `detector does not emit event on post that does contain two remote sones`() {
		val post = createPost("text mentions sone://${remoteSone1.id} and sone://${remoteSone2.id}.")
		eventBus.post(NewPostFoundEvent(post))
		assertThat(capturedFoundEvents, emptyIterable())
	}

	@Test
	fun `detector emits event on post that contains links to a remote and a local sone`() {
		val post = createPost("text mentions sone://${localSone1.id} and sone://${remoteSone2.id}.")
		eventBus.post(NewPostFoundEvent(post))
		assertThat(capturedFoundEvents, contains(MentionOfLocalSoneFoundEvent(post)))
	}

	@Test
	fun `detector emits one event on post that contains two links to the same local sone`() {
		val post = createPost("text mentions sone://${localSone1.id} and sone://${localSone1.id}.")
		eventBus.post(NewPostFoundEvent(post))
		assertThat(capturedFoundEvents, contains(MentionOfLocalSoneFoundEvent(post)))
	}

	@Test
	fun `detector emits one event on post that contains links to two local sones`() {
		val post = createPost("text mentions sone://${localSone1.id} and sone://${localSone2.id}.")
		eventBus.post(NewPostFoundEvent(post))
		assertThat(capturedFoundEvents, contains(MentionOfLocalSoneFoundEvent(post)))
	}

	@Test
	fun `detector does not emit event for post by local sone`() {
		val post = createPost("text mentions sone://${localSone1.id} and sone://${localSone2.id}.", localSone1)
		eventBus.post(NewPostFoundEvent(post))
		assertThat(capturedFoundEvents, emptyIterable())
	}

	@Test
	fun `detector does not emit event for reply that contains no sones`() {
		val reply = emptyPostReply()
		eventBus.post(NewPostReplyFoundEvent(reply))
		assertThat(capturedFoundEvents, emptyIterable())
	}

	@Test
	fun `detector does not emit event for reply that contains two links to remote sones`() {
		val reply = emptyPostReply("text mentions sone://${remoteSone1.id} and sone://${remoteSone2.id}.")
		eventBus.post(NewPostReplyFoundEvent(reply))
		assertThat(capturedFoundEvents, emptyIterable())
	}

	@Test
	fun `detector emits event on reply that contains links to a remote and a local sone`() {
		val post = createPost()
		val reply = emptyPostReply("text mentions sone://${remoteSone1.id} and sone://${localSone1.id}.", post)
		eventBus.post(NewPostReplyFoundEvent(reply))
		assertThat(capturedFoundEvents, contains(MentionOfLocalSoneFoundEvent(post)))
	}

	@Test
	fun `detector emits one event on reply that contains two links to the same local sone`() {
		val post = createPost()
		val reply = emptyPostReply("text mentions sone://${localSone1.id} and sone://${localSone1.id}.", post)
		eventBus.post(NewPostReplyFoundEvent(reply))
		assertThat(capturedFoundEvents, contains(MentionOfLocalSoneFoundEvent(post)))
	}

	@Test
	fun `detector emits one event on reply that contains two links to local sones`() {
		val post = createPost()
		val reply = emptyPostReply("text mentions sone://${localSone1.id} and sone://${localSone2.id}.", post)
		eventBus.post(NewPostReplyFoundEvent(reply))
		assertThat(capturedFoundEvents, contains(MentionOfLocalSoneFoundEvent(post)))
	}

	@Test
	fun `detector does not emit event for reply by local sone`() {
		val reply = emptyPostReply("text mentions sone://${localSone1.id} and sone://${localSone2.id}.", sone = localSone1)
		eventBus.post(NewPostReplyFoundEvent(reply))
		assertThat(capturedFoundEvents, emptyIterable())
	}

	@Test
	fun `detector does not emit event for reply without post`() {
		val reply = emptyPostReply("text mentions sone://${localSone1.id} and sone://${localSone2.id}.", post = null)
		eventBus.post(NewPostReplyFoundEvent(reply))
		assertThat(caughtExceptions, emptyIterable())
		assertThat(capturedFoundEvents, emptyIterable())
	}

	@Test
	fun `detector does not emit removed event when a post without mention is removed`() {
		val post = createPost()
		eventBus.post(PostRemovedEvent(post))
		assertThat(capturedRemovedEvents, emptyIterable())
	}

	@Test
	fun `detector does emit removed event when post with mention is removed`() {
		val post = createPost("sone://${localSone1.id}")
		eventBus.post(NewPostFoundEvent(post))
		eventBus.post(PostRemovedEvent(post))
		assertThat(capturedRemovedEvents, contains(MentionOfLocalSoneRemovedEvent(post)))
	}

	@Test
	fun `detector does not emit removed event when a post without mention is marked as known`() {
		val post = createPost()
		eventBus.post(MarkPostKnownEvent(post))
		assertThat(capturedRemovedEvents, emptyIterable())
	}

	@Test
	fun `detector does emit removed event when post with mention is marked as known`() {
		val post = createPost("sone://${localSone1.id}")
		eventBus.post(NewPostFoundEvent(post))
		eventBus.post(MarkPostKnownEvent(post))
		assertThat(capturedRemovedEvents, contains(MentionOfLocalSoneRemovedEvent(post)))
	}

	@Test
	fun `detector does emit removed event when reply with mention is removed and no more mentions in that post exist`() {
		val post = createPost()
		val reply = emptyPostReply("sone://${localSone1.id}", post)
		postReplyProvider.postReplies[post.id] = listOf(reply)
		eventBus.post(NewPostReplyFoundEvent(reply))
		eventBus.post(PostReplyRemovedEvent(reply))
		assertThat(capturedRemovedEvents, contains(MentionOfLocalSoneRemovedEvent(post)))
	}

	@Test
	fun `detector does not emit removed event when reply with mention is removed and post mentions local sone`() {
		val post = createPost("sone://${localSone1.id}")
		val reply = emptyPostReply("sone://${localSone1.id}", post)
		eventBus.post(NewPostReplyFoundEvent(reply))
		eventBus.post(PostReplyRemovedEvent(reply))
		assertThat(capturedRemovedEvents, emptyIterable())
	}

	@Test
	fun `detector does emit removed event when reply with mention is removed and post mentions local sone but is known`() {
		val post = createPost("sone://${localSone1.id}", known = true)
		val reply = emptyPostReply("sone://${localSone1.id}", post)
		eventBus.post(NewPostReplyFoundEvent(reply))
		eventBus.post(PostReplyRemovedEvent(reply))
		assertThat(capturedRemovedEvents, contains(MentionOfLocalSoneRemovedEvent(post)))
	}

	@Test
	fun `detector does not emit removed event when reply with mention is removed and post has other replies with mentions`() {
		val post = createPost()
		val reply1 = emptyPostReply("sone://${localSone1.id}", post)
		val reply2 = emptyPostReply("sone://${localSone1.id}", post)
		postReplyProvider.postReplies[post.id] = listOf(reply1, reply2)
		eventBus.post(NewPostReplyFoundEvent(reply1))
		eventBus.post(PostReplyRemovedEvent(reply1))
		assertThat(capturedRemovedEvents, emptyIterable())
	}

	@Test
	fun `detector does emit removed event when reply with mention is removed and post has other replies with mentions which are known`() {
		val post = createPost()
		val reply1 = emptyPostReply("sone://${localSone1.id}", post)
		val reply2 = emptyPostReply("sone://${localSone1.id}", post, known = true)
		postReplyProvider.postReplies[post.id] = listOf(reply1, reply2)
		eventBus.post(NewPostReplyFoundEvent(reply1))
		eventBus.post(PostReplyRemovedEvent(reply1))
		assertThat(capturedRemovedEvents, contains(MentionOfLocalSoneRemovedEvent(post)))
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

private class TestPostReplyProvider : PostReplyProvider {

	val replies = mutableMapOf<String, PostReply>()
	val postReplies = mutableMapOf<String, List<PostReply>>()

	override fun getPostReply(id: String) = replies[id]
	override fun getReplies(postId: String) = postReplies[postId] ?: emptyList()

}
