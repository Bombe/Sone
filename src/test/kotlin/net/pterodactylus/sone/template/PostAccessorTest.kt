package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.util.template.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*

/**
 * Unit test for [PostAccessor].
 */
class PostAccessorTest {

	private val core = mock<Core>()
	private val accessor = PostAccessor(core)
	private val post = mock<Post>()
	private val now = System.currentTimeMillis()

	@Before
	fun setupPost() {
		whenever(post.id).thenReturn("post-id")
	}

	@Test
	fun `accessor returns the correct replies`() {
		val replies = listOf(
				createPostReply(2000),
				createPostReply(-1000),
				createPostReply(-2000),
				createPostReply(-3000),
				createPostReply(-4000)
		)
		whenever(core.getReplies("post-id")).thenReturn(replies)
		val repliesForPost = accessor[null, post, "replies"] as Collection<PostReply>
		assertThat(repliesForPost, contains(
				replies[1],
				replies[2],
				replies[3],
				replies[4]
		))
	}

	private fun createPostReply(timeOffset: Long) = mock<PostReply>().apply {
		whenever(time).thenReturn(now + timeOffset)
	}

	@Test
	fun `accessor returns the liking sones`() {
		val sones = setOf<Sone>()
		whenever(core.getLikes(post)).thenReturn(sones)
		val likingSones = accessor[null, post, "likes"] as Set<Sone>
		assertThat(likingSones, equalTo(sones))
	}

	@Test
	fun `accessor returns whether the current sone liked a post`() {
		val sone = mock<Sone>()
		whenever(sone.isLikedPostId("post-id")).thenReturn(true)
		val templateContext = TemplateContext()
		templateContext["currentSone"] = sone
		assertThat(accessor[templateContext, post, "liked"], equalTo<Any>(true))
	}

	@Test
	fun `accessor returns false if post is not liked`() {
		val sone = mock<Sone>()
		val templateContext = TemplateContext()
		templateContext["currentSone"] = sone
		assertThat(accessor[templateContext, post, "liked"], equalTo<Any>(false))
	}

	@Test
	fun `accessor returns false if there is no current sone`() {
		val templateContext = TemplateContext()
		assertThat(accessor[templateContext, post, "liked"], equalTo<Any>(false))
	}

	@Test
	fun `accessor returns that not known post is new`() {
		assertThat(accessor[null, post, "new"], equalTo<Any>(true))
	}

	@Test
	fun `accessor returns that known post is not new`() {
		whenever(post.isKnown).thenReturn(true)
		assertThat(accessor[null, post, "new"], equalTo<Any>(false))
	}

	@Test
	fun `accessor returns if post is bookmarked`() {
		whenever(core.isBookmarked(post)).thenReturn(true)
		assertThat(accessor[null, post, "bookmarked"], equalTo<Any>(true))
	}

	@Test
	fun `accessor returns other properties`() {
		assertThat(accessor[null, post, "hashCode"], equalTo<Any>(post.hashCode()))
	}

}
