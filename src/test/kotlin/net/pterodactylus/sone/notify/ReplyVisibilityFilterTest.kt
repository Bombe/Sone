package net.pterodactylus.sone.notify

import com.google.common.base.Optional
import com.google.inject.Guice
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.freenet.wot.OwnIdentity
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.sameInstance
import org.junit.Test

/**
 * Unit test for [ReplyVisibilityFilterTest].
 */
class ReplyVisibilityFilterTest {

	private val postVisibilityFilter = mock<PostVisibilityFilter>()
	private val replyVisibilityFilter = ReplyVisibilityFilter(postVisibilityFilter)
	private val localSone = mock<Sone>()
	private val localIdentity = mock<OwnIdentity>()
	private val post = mock<Post>()
	private val postReply = mock<PostReply>()

	@Test
	fun `reply visibility filter is only created once`() {
		val injector = Guice.createInjector()
		val firstFilter = injector.getInstance<ReplyVisibilityFilter>()
		val secondFilter = injector.getInstance<ReplyVisibilityFilter>()
		assertThat(firstFilter, sameInstance(secondFilter))
	}

	private fun makePostPresent() {
		whenever(postReply.post).thenReturn(Optional.of(post))
	}

	@Test
	fun `reply is not visible if post is not visible`() {
		makePostPresent()
		assertThat(replyVisibilityFilter.isReplyVisible(localSone, postReply), equalTo(false))
	}

	private fun makePostAbsent() {
		whenever(postReply.post).thenReturn(Optional.absent())
	}

	@Test
	fun `reply is not visible if post is not present`() {
		makePostAbsent()
		assertThat(replyVisibilityFilter.isReplyVisible(localSone, postReply), equalTo(false))
	}

	private fun makePostPresentAndVisible() {
		makePostPresent()
		whenever(postVisibilityFilter.isPostVisible(localSone, post)).thenReturn(true)
	}

	private fun makeReplyComeFromFuture() {
		whenever(postReply.time).thenReturn(System.currentTimeMillis() + 1000)
	}

	@Test
	fun `reply is not visible if it is from the future`() {
		makePostPresentAndVisible()
		makeReplyComeFromFuture()
		assertThat(replyVisibilityFilter.isReplyVisible(localSone, postReply), equalTo(false))
	}

	@Test
	fun `reply is visible if it is not from the future`() {
		makePostPresentAndVisible()
		assertThat(replyVisibilityFilter.isReplyVisible(localSone, postReply), equalTo(true))
	}

	@Test
	fun `predicate correctly recognizes visible reply`() {
		makePostPresentAndVisible()
		assertThat(replyVisibilityFilter.isVisible(localSone).test(postReply), equalTo(true))
	}

	@Test
	fun `predicate correctly recognizes not visible reply`() {
		makePostPresentAndVisible()
		makeReplyComeFromFuture()
		assertThat(replyVisibilityFilter.isVisible(localSone).test(postReply), equalTo(false))
	}

	init {
		whenever(localSone.id).thenReturn(LOCAL_ID)
		whenever(localSone.isLocal).thenReturn(true)
		whenever(localSone.identity).thenReturn(localIdentity)
		whenever(post.recipientId).thenReturn(Optional.absent())
	}

}

private const val LOCAL_ID = "local-id"
