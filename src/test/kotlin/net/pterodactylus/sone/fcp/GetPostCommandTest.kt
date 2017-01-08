package net.pterodactylus.sone.fcp

import freenet.support.SimpleFieldSet
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.asOptional
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
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
	private val post = createPost()

	override fun createCommand(core: Core) = GetPostCommand(core)

	@Before
	fun setupPostWithLikesAndReplies() {
		whenever(core.getPost("ValidPostId")).thenReturn(post.asOptional())
		val sone1 = mock<Sone>().apply { whenever(id).thenReturn("Sone1") }
		val sone2 = mock<Sone>().apply { whenever(id).thenReturn("Sone2") }
		whenever(core.getLikes(post)).thenReturn(setOf(sone1, sone2))
		val replies = listOf(
				createPostReply("ReplyId1", sone1, 1000, "Reply 1"),
				createPostReply("ReplyId2", sone2, 2000, "Reply 2")
		)
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
		parameters.putSingle("Post", "InvalidPostId")
		executeCommandAndExpectFcpException()
	}

	private fun createPostReply(id: String, sone: Sone, time: Long, text: String) = mock<PostReply>().apply {
		whenever(this.id).thenReturn(id)
		whenever(this.sone).thenReturn(sone)
		whenever(this.time).thenReturn(time)
		whenever(this.text).thenReturn(text)
	}

	private fun createPost() = mock<Post>().apply {
		whenever(id).thenReturn("ValidPostId")
		whenever(this.sone).thenReturn(this@GetPostCommandTest.sone)
		whenever(recipientId).thenReturn(null.asOptional())
		whenever(time).thenReturn(1000)
		whenever(text).thenReturn("Post Text\r\nSecond \\Line")
	}

	private fun verifyPostWithLikes(replyParameters: SimpleFieldSet) {
		assertThat(replyParameters["Message"], equalTo("Post"))
		assertThat(replyParameters["Post.ID"], equalTo("ValidPostId"))
		assertThat(replyParameters["Post.Sone"], equalTo("SoneId"))
		assertThat(replyParameters["Post.Time"], equalTo("1000"))
		assertThat(replyParameters["Post.Text"], equalTo("Post Text\\r\\nSecond \\\\Line"))
		assertThat(replyParameters["Post.Likes.Count"], equalTo("2"))
		assertThat((0..1).map { replyParameters["Post.Likes.$it.ID"] }, containsInAnyOrder("Sone1", "Sone2"))
	}

	private fun verifyReplies(replyParameters: SimpleFieldSet) {
		assertThat(replyParameters["Post.Replies.Count"], equalTo("2"))
		assertThat(replyParameters["Post.Replies.0.ID"], equalTo("ReplyId1"))
		assertThat(replyParameters["Post.Replies.0.Sone"], equalTo("Sone1"))
		assertThat(replyParameters["Post.Replies.0.Time"], equalTo("1000"))
		assertThat(replyParameters["Post.Replies.0.Text"], equalTo("Reply 1"))
		assertThat(replyParameters["Post.Replies.1.ID"], equalTo("ReplyId2"))
		assertThat(replyParameters["Post.Replies.1.Sone"], equalTo("Sone2"))
		assertThat(replyParameters["Post.Replies.1.Time"], equalTo("2000"))
		assertThat(replyParameters["Post.Replies.1.Text"], equalTo("Reply 2"))
	}

	@Test
	fun `request with valid post parameter returns post response`() {
		parameters.putSingle("Post", "ValidPostId")
		val replyParameters = command.execute(parameters).replyParameters
		verifyPostWithLikes(replyParameters)
		assertThat(replyParameters["Post.Recipient"], nullValue())
		verifyReplies(replyParameters)
	}

	@Test
	fun `request with valid post parameter without replies returns post response without replies`() {
		parameters.putSingle("Post", "ValidPostId")
		parameters.putSingle("IncludeReplies", "false")
		val replyParameters = command.execute(parameters).replyParameters
		verifyPostWithLikes(replyParameters)
		assertThat(replyParameters["Post.Recipient"], nullValue())
		assertThat(replyParameters["Post.Replies.Count"], nullValue())
	}

	@Test
	fun `request with valid post parameter returns post response with recipient`() {
		parameters.putSingle("Post", "ValidPostId")
		whenever(post.recipientId).thenReturn("Sone2".asOptional())
		val replyParameters = command.execute(parameters).replyParameters
		verifyPostWithLikes(replyParameters)
		assertThat(replyParameters["Post.Recipient"], equalTo("Sone2"))
		verifyReplies(replyParameters)
	}

	@Test
	fun `request with valid post parameter without replies returns post response without replies but with recipient`() {
		parameters.putSingle("Post", "ValidPostId")
		parameters.putSingle("IncludeReplies", "false")
		whenever(post.recipientId).thenReturn("Sone2".asOptional())
		val replyParameters = command.execute(parameters).replyParameters
		verifyPostWithLikes(replyParameters)
		assertThat(replyParameters["Post.Recipient"], equalTo("Sone2"))
		assertThat(replyParameters["Post.Replies.Count"], nullValue())
	}

}
