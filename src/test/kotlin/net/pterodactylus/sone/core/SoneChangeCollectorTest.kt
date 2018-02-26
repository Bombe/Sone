package net.pterodactylus.sone.core

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.emptyIterable
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 * Unit test for [SoneChangeCollectorTest].
 */
class SoneChangeCollectorTest {

	private val oldSone = mock<Sone>()
	private val newSone = mock<Sone>()
	private val changeCollector = SoneChangeCollector(oldSone)

	@Test
	fun `new posts are correctly turned into events`() {
		val posts = listOf(mock<Post>(), mock(), mock())
		whenever(newSone.posts).thenReturn(posts)
		changeCollector.newPostEvent { it.takeIf { it != posts[1] } }
		assertThat(changeCollector.detectChanges(newSone), containsInAnyOrder<Any>(posts[0], posts[2]))
	}

	@Test
	fun `actions can be performed on new post without being returned`() {
		val posts = listOf(mock<Post>(), mock(), mock())
		val counter = AtomicInteger(0)
		whenever(newSone.posts).thenReturn(posts.slice(0..2))
		whenever(oldSone.posts).thenReturn(posts.slice(2..2))
		changeCollector.onNewPost { counter.incrementAndGet() }
		assertThat(changeCollector.detectChanges(newSone), emptyIterable())
		assertThat(counter.get(), equalTo(2))
	}

	@Test
	fun `removed posts are correctly turned into events`() {
		val posts = listOf(mock<Post>(), mock(), mock())
		whenever(oldSone.posts).thenReturn(posts)
		changeCollector.removedPostEvent { it.takeIf { it != posts[1] } }
		assertThat(changeCollector.detectChanges(newSone), containsInAnyOrder<Any>(posts[0], posts[2]))
	}

	@Test
	fun `new post replies are correctly turned into events`() {
		val postReplies = listOf(mock<PostReply>(), mock(), mock())
		whenever(newSone.replies).thenReturn(postReplies.toSet())
		changeCollector.newPostReplyEvent { it.takeIf { it != postReplies[1] } }
		assertThat(changeCollector.detectChanges(newSone), containsInAnyOrder<Any>(postReplies[0], postReplies[2]))
	}

	@Test
	fun `actions can be performed on new replies without being returned`() {
		val replies = listOf(mock<PostReply>(), mock(), mock())
		val counter = AtomicInteger(0)
		whenever(newSone.replies).thenReturn(replies.slice(0..2).toSet())
		whenever(oldSone.replies).thenReturn(replies.slice(2..2).toSet())
		changeCollector.onNewPostReply { counter.incrementAndGet() }
		assertThat(changeCollector.detectChanges(newSone), emptyIterable())
		assertThat(counter.get(), equalTo(2))
	}

	@Test
	fun `removed post replies are correctly turned into events`() {
		val postReplies = listOf(mock<PostReply>(), mock(), mock())
		whenever(oldSone.replies).thenReturn(postReplies.toSet())
		changeCollector.onRemovedPostReply { it.takeIf { it != postReplies[1] } }
		assertThat(changeCollector.detectChanges(newSone), containsInAnyOrder<Any>(postReplies[0], postReplies[2]))
	}

}
