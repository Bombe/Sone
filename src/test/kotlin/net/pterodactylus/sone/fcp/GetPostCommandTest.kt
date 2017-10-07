package net.pterodactylus.sone.fcp

import freenet.support.SimpleFieldSet
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.utils.asOptional
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test

/**
 * Unit test for [GetPostCommand].
 */
class GetPostCommandTest : SoneCommandTest() {

	private val sone = mock<Sone>().apply {
		whenever(id).thenReturn("SoneId")
	}
	private val post = createPost("ValidPostId", sone, null, 1000, "Post Text\r\nSecond \\Line")
	private val sone1 = mock<Sone>().apply { whenever(id).thenReturn("Sone1") }
	private val sone2 = mock<Sone>().apply { whenever(id).thenReturn("Sone2") }
	private val postReply1 = createReply("ReplyId1", sone1, post, 1000, "Reply 1")
	private val postReply2 = createReply("ReplyId2", sone2, post, 2000, "Reply 2")

	override fun createCommand(core: Core) = GetPostCommand(core)

	@Before
	fun setupPostWithLikesAndReplies() {
		whenever(core.getPost("ValidPostId")).thenReturn(post.asOptional())
		whenever(core.getLikes(post)).thenReturn(setOf(sone1, sone2))
		val replies = listOf(postReply1, postReply2)
		whenever(core.getReplies("ValidPostId")).thenReturn(replies)
	}

	@Test
	fun `command does not require write access`() {
		assertThat(command.requiresWriteAccess(), equalTo(false))
	}

	@Test
	fun `request without any parameter results in fcp exception`() {
		requestWithoutAnyParameterResultsInFcpException()
	}

	@Test
	fun `request with invalid post parameter results in fcp exception`() {
		parameters += "Post" to "InvalidPostId"
		executeCommandAndExpectFcpException()
	}

	private fun verifyPostWithLikes(replyParameters: SimpleFieldSet) {
		assertThat(replyParameters["Message"], equalTo("Post"))
		assertThat(replyParameters.parsePost("Post."), matchesPost(post))
		assertThat(replyParameters["Post.Likes.Count"], equalTo("2"))
		assertThat((0..1).map { replyParameters["Post.Likes.$it.ID"] }, containsInAnyOrder("Sone1", "Sone2"))
	}

	private fun verifyReplies(replyParameters: SimpleFieldSet) {
		assertThat(replyParameters["Post.Replies.Count"], equalTo("2"))
		assertThat(replyParameters.parsePost("Post.Replies.0."), matchesReply(postReply1))
		assertThat(replyParameters.parsePost("Post.Replies.1."), matchesReply(postReply2))
	}

	@Test
	fun `request with valid post parameter returns post response`() {
		parameters += "Post" to "ValidPostId"
		val replyParameters = command.execute(parameters).replyParameters
		verifyPostWithLikes(replyParameters)
		assertThat(replyParameters["Post.Recipient"], nullValue())
		verifyReplies(replyParameters)
	}

	@Test
	fun `request with valid post parameter without replies returns post response without replies`() {
		parameters += "Post" to "ValidPostId"
		parameters += "IncludeReplies" to "false"
		val replyParameters = command.execute(parameters).replyParameters
		verifyPostWithLikes(replyParameters)
		assertThat(replyParameters["Post.Recipient"], nullValue())
		assertThat(replyParameters["Post.Replies.Count"], nullValue())
	}

	@Test
	fun `request with valid post parameter returns post response with recipient`() {
		parameters += "Post" to "ValidPostId"
		whenever(post.recipientId).thenReturn("Sone2".asOptional())
		val replyParameters = command.execute(parameters).replyParameters
		verifyPostWithLikes(replyParameters)
		assertThat(replyParameters["Post.Recipient"], equalTo("Sone2"))
		verifyReplies(replyParameters)
	}

	@Test
	fun `request with valid post parameter without replies returns post response without replies but with recipient`() {
		parameters += "Post" to "ValidPostId"
		parameters += "IncludeReplies" to "false"
		whenever(post.recipientId).thenReturn("Sone2".asOptional())
		val replyParameters = command.execute(parameters).replyParameters
		verifyPostWithLikes(replyParameters)
		assertThat(replyParameters["Post.Recipient"], equalTo("Sone2"))
		assertThat(replyParameters["Post.Replies.Count"], nullValue())
	}

}
