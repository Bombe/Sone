package net.pterodactylus.sone.notify

import com.google.inject.Guice
import net.pterodactylus.sone.test.createLocalSone
import net.pterodactylus.sone.test.createPost
import net.pterodactylus.sone.test.createPostReply
import net.pterodactylus.sone.test.verifySingletonInstance
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [ReplyVisibilityFilterTest].
 */
class ReplyVisibilityFilterTest {

	private val replyVisibilityFilter = DefaultReplyVisibilityFilter(showAllPosts)
	private val localSone = createLocalSone()
	private val post = createPost()

	@Test
	fun `reply visibility filter is only created once`() {
		val injector = Guice.createInjector()
		injector.verifySingletonInstance<DefaultReplyVisibilityFilter>()
	}

	@Test
	fun `reply is not visible if post is not visible`() {
		val postReply = createPostReply(post = post)
		val replyVisibilityFilter = DefaultReplyVisibilityFilter(showNoPosts)
		assertThat(replyVisibilityFilter.isReplyVisible(null, postReply), equalTo(false))
	}

	@Test
	fun `reply is not visible if post is not present`() {
		val postReply = createPostReply(post = null)
		assertThat(replyVisibilityFilter.isReplyVisible(null, postReply), equalTo(false))
	}

	@Test
	fun `reply is not visible if it is from the future`() {
		val postReply = createPostReply(post = post, time = System.currentTimeMillis() + 100000)
		assertThat(replyVisibilityFilter.isReplyVisible(null, postReply), equalTo(false))
	}

	@Test
	fun `reply is visible if it is not from the future`() {
		val postReply = createPostReply(post = post)
		assertThat(replyVisibilityFilter.isReplyVisible(null, postReply), equalTo(true))
	}

	@Test
	fun `predicate correctly recognizes visible reply`() {
		val postReply = createPostReply(post = post)
		assertThat(replyVisibilityFilter.isVisible(localSone).test(postReply), equalTo(true))
	}

	@Test
	fun `predicate correctly recognizes not visible reply`() {
		val postReply = createPostReply(post = post, time = System.currentTimeMillis() + 100000)
		assertThat(replyVisibilityFilter.isVisible(localSone).test(postReply), equalTo(false))
	}

}
