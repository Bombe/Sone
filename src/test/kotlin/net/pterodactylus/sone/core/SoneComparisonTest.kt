package net.pterodactylus.sone.core

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*

class SoneComparsisonTest {

	private val oldSone = mock<Sone>()
	private val newSone = mock<Sone>()

	private val oldPost = mock<Post>()
	private val removedPost = mock<Post>()
	private val newPost = mock<Post>()
	private val oldPostReply = mock<PostReply>()
	private val removedPostReply = mock<PostReply>()
	private val newPostReply = mock<PostReply>()

	init {
		whenever(oldSone.posts).thenReturn(listOf(oldPost, removedPost))
		whenever(newSone.posts).thenReturn(listOf(oldPost, newPost))
		whenever(oldSone.replies).thenReturn(setOf(oldPostReply, removedPostReply))
		whenever(newSone.replies).thenReturn(setOf(oldPostReply, newPostReply))
	}

	private val soneComparison = SoneComparison(oldSone, newSone)

	@Test
	fun `new posts are identified correctly`() {
		assertThat(soneComparison.newPosts, contains(newPost))
	}

	@Test
	fun `removed posts are identified correctly`() {
		assertThat(soneComparison.removedPosts, contains(removedPost))
	}

	@Test
	fun `new post replies are identified correctly`() {
		assertThat(soneComparison.newPostReplies, contains(newPostReply))
	}

	@Test
	fun `removed post replies are identified correctly`() {
		assertThat(soneComparison.removedPostReplies, contains(removedPostReply))
	}

}
