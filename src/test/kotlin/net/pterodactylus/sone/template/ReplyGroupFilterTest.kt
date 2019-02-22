package net.pterodactylus.sone.template

import com.google.common.base.Optional
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.template.TemplateContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test

/**
 * Unit test for [ReplyGroupFilter].
 */
class ReplyGroupFilterTest {

	private val filter = ReplyGroupFilter()
	private val replies = mutableListOf<PostReply>()
	private val posts = mutableListOf<Post>()
	private val sones = mutableListOf<Sone>()
	private val templateContext = mock<TemplateContext>()

	@Before
	fun setupReplies() {
		repeat(5) {
			sones += mock<Sone>()
		}
		(0..7).forEach {
			posts += mock<Post>()
			whenever(posts[it].sone).thenReturn(sones[(it + 1) % sones.size])
		}
		(0..10).forEach {
			replies += mock<PostReply>()
			whenever(replies[it].sone).thenReturn(sones[it % sones.size])
			whenever(replies[it].post).thenReturn(Optional.of(posts[it % posts.size]))
		}
	}

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `replies are grouped correctly`() {
		val groupReplies = filter.format(templateContext, replies, emptyMap()) as Map<Post, Map<String, *>>
		assertThat(groupReplies.keys, equalTo(posts.toSet()))
		verifyPostRepliesAndSones(groupReplies, 0, listOf(0, 3), listOf(0, 8))
		verifyPostRepliesAndSones(groupReplies, 1, listOf(1, 4), listOf(1, 9))
		verifyPostRepliesAndSones(groupReplies, 2, listOf(2, 0), listOf(2, 10))
		verifyPostRepliesAndSones(groupReplies, 3, listOf(3), listOf(3))
		verifyPostRepliesAndSones(groupReplies, 4, listOf(4), listOf(4))
		verifyPostRepliesAndSones(groupReplies, 5, listOf(0), listOf(5))
		verifyPostRepliesAndSones(groupReplies, 6, listOf(1), listOf(6))
		verifyPostRepliesAndSones(groupReplies, 7, listOf(2), listOf(7))
	}

	@Suppress("UNCHECKED_CAST")
	private fun verifyPostRepliesAndSones(groupReplies: Map<Post, Map<String, *>>, postIndex: Int, soneIndices: List<Int>, replyIndices: List<Int>) {
		assertThat(groupReplies[posts[postIndex]]!!["sones"] as Iterable<Sone>, containsInAnyOrder(*soneIndices.map { sones[it] }.toTypedArray()))
		assertThat(groupReplies[posts[postIndex]]!!["replies"] as Iterable<PostReply>, containsInAnyOrder(*replyIndices.map { replies[it] }.toTypedArray()))
	}

}
