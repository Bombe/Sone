package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.utils.*
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
	fun `reply sone for remote post without replies is current sone`() {
		val post = mockPostFrom(remoteSone)
		assertThat(accessor[templateContext, post, "replySone"], equalTo<Any>(currentSone))
	}

	@Test
	fun `reply sone for remote post with remote replies is current sone`() {
		val post = mockPostFrom(remoteSone)
		val replies = listOf(mockReplyFrom(remoteSone), mockReplyFrom(remoteSone))
		whenever(core.getReplies("post-id")).thenReturn(replies)
		assertThat(accessor[templateContext, post, "replySone"], equalTo<Any>(currentSone))
	}

	@Test
	fun `reply sone for remote post with remote and one local replies is sone of local reply`() {
		val post = mockPostFrom(remoteSone)
		val localSone = mockLocalSone()
		val replies = listOf(mockReplyFrom(remoteSone), mockReplyFrom(localSone))
		whenever(core.getReplies("post-id")).thenReturn(replies)
		assertThat(accessor[templateContext, post, "replySone"], equalTo<Any>(localSone))
	}

	@Test
	fun `reply sone for remote post with remote and several local replies is sone of latest local reply`() {
		val post = mockPostFrom(remoteSone)
		val localSone1 = mockLocalSone()
		val localSone2 = mockLocalSone()
		val replies = listOf(mockReplyFrom(remoteSone), mockReplyFrom(localSone1), mockReplyFrom(localSone2))
		whenever(core.getReplies("post-id")).thenReturn(replies)
		assertThat(accessor[templateContext, post, "replySone"], equalTo<Any>(localSone2))
	}

	@Test
	fun `reply sone for local post without replies is post sone`() {
		val localSone = mockLocalSone()
		val post = mockPostFrom(localSone)
		assertThat(accessor[templateContext, post, "replySone"], equalTo<Any>(localSone))
	}

	@Test
	fun `reply sone for local post with remote replies is local sone`() {
		val localSone = mockLocalSone()
		val post = mockPostFrom(localSone)
		val replies = listOf(mockReplyFrom(remoteSone), mockReplyFrom(remoteSone))
		whenever(core.getReplies("post-id")).thenReturn(replies)
		assertThat(accessor[templateContext, post, "replySone"], equalTo<Any>(localSone))
	}

	@Test
	fun `reply sone for local post with remote and one local replies is local reply sone`() {
		val localSone1 = mockLocalSone()
		val post = mockPostFrom(localSone1)
		val localSone2 = mockLocalSone()
		val replies = listOf(mockReplyFrom(remoteSone), mockReplyFrom(localSone2))
		whenever(core.getReplies("post-id")).thenReturn(replies)
		assertThat(accessor[templateContext, post, "replySone"], equalTo<Any>(localSone2))
	}

	@Test
	fun `reply sone for local post with remote and several local replies is latest local reply sone`() {
		val localSone1 = mockLocalSone()
		val post = mockPostFrom(localSone1)
		val localSone2 = mockLocalSone()
		val localSone3 = mockLocalSone()
		val replies = listOf(mockReplyFrom(remoteSone), mockReplyFrom(localSone2), mockReplyFrom(localSone3))
		whenever(core.getReplies("post-id")).thenReturn(replies)
		assertThat(accessor[templateContext, post, "replySone"], equalTo<Any>(localSone3))
	}

	@Test
	fun `reply sone for post directed at local sone is local sone`() {
		val localSone = mockLocalSone()
		val post = mockPostFrom(remoteSone, localSone)
		assertThat(accessor[templateContext, post, "replySone"], equalTo<Any>(localSone))
	}


	@Test
	fun `accessor returns other properties`() {
		assertThat(accessor[null, post, "hashCode"], equalTo<Any>(post.hashCode()))
	}

}

private val currentSone = mock<Sone>()
private val remoteSone = mock<Sone>()
private fun mockLocalSone() = mock<Sone>().apply { whenever(isLocal).thenReturn(true) }

private val templateContext = TemplateContext().apply {
	this["currentSone"] = currentSone
}

private fun mockPostFrom(sone: Sone, recipient: Sone? = null) = mock<Post>().apply {
	whenever(id).thenReturn("post-id")
	whenever(this.sone).thenReturn(sone)
	whenever(this.recipient).thenReturn(recipient.asOptional())
}

private fun mockReplyFrom(sone: Sone) = mock<PostReply>().apply { whenever(this.sone).thenReturn(sone) }
