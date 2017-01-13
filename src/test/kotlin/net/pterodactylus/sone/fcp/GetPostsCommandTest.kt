package net.pterodactylus.sone.fcp

import freenet.support.SimpleFieldSet
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.test.asOptional
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [GetPostsCommand].
 */
class GetPostsCommandTest : SoneCommandTest() {

	private val sone1 = createSone("Sone1", "Sone1", "Sone", "#1", 1000)
	private val sone2 = createSone("Sone2", "Sone2", "Sone", "#2", 2000)
	private val post1 = createPost("Post1", remoteSone, null, 1000, "Post \\1\n")
	private val post2 = createPost("Post2", localSone, null, 2000, "Post \\2\r")
	private val post2Reply1 = createReply("Post2Reply1", localSone, post2, 2000, "Reply 1")
	private val post2Reply2 = createReply("Post2Reply2", remoteSone, post2, 3000, "Reply 2")

	override fun createCommand(core: Core) = GetPostsCommand(core)

	@Test
	fun `command does not require write access`() {
		assertThat(command.requiresWriteAccess(), equalTo(false))
	}

	@Test
	fun `request without any parameters results in fcp exception`() {
		requestWithoutAnyParameterResultsInFcpException()
	}

	@Test
	fun `request with empty Sone parameter results in fcp exception`() {
		requestWithEmptySoneParameterResultsInFcpException()
	}

	@Test
	fun `request with invalid Sone parameter results in fcp exception`() {
		requestWithInvalidSoneParameterResultsInFcpException()
	}

	private fun setupPostsAndReplies() {
		whenever(core.getLikes(post1)).thenReturn(setOf(localSone, sone1))
		whenever(core.getLikes(post2Reply1)).thenReturn(setOf(remoteSone, sone2))
		whenever(core.getReplies("Post2")).thenReturn(listOf(post2Reply1, post2Reply2))
		whenever(localSone.id).thenReturn("LocalSone")
		whenever(remoteSone.id).thenReturn("RemoteSone")
		whenever(core.getSone("LocalSone")).thenReturn(localSone.asOptional())
		whenever(core.getSone("ValidSoneId")).thenReturn(remoteSone.asOptional())
		whenever(remoteSone.posts).thenReturn(listOf(post2, post1))
		parameters.putSingle("Sone", "ValidSoneId")
	}

	private fun verifyFirstPost(replyParameters: SimpleFieldSet, index: Int = 0) {
		assertThat(replyParameters.parsePost("Posts.$index."), matchesPost(post2))
		assertThat(replyParameters["Posts.$index.Replies.Count"], equalTo("2"))
		assertThat(replyParameters.parseReply("Posts.$index.Replies.0."), matchesReply(post2Reply1))
		assertThat(replyParameters["Posts.$index.Replies.0.Likes.Count"], equalTo("2"))
		assertThat((0..1).map { replyParameters["Posts.$index.Replies.0.Likes.$it.ID"] }, containsInAnyOrder("RemoteSone", "Sone2"))
		assertThat(replyParameters.parseReply("Posts.$index.Replies.1."), matchesReply(post2Reply2))
	}

	private fun verifySecondPost(replyParameters: SimpleFieldSet, index: Int = 1) {
		assertThat(replyParameters.parsePost("Posts.$index."), matchesPost(post1))
		assertThat(replyParameters["Posts.$index.Likes.Count"], equalTo("2"))
		assertThat((0..1).map { replyParameters["Posts.$index.Likes.$it.ID"] }, containsInAnyOrder("LocalSone", "Sone1"))
	}

	@Test
	fun `request with valid sone parameter lists all posts of the sone`() {
		setupPostsAndReplies()

		val replyParameters = command.execute(parameters).replyParameters

		assertThat(replyParameters["Message"], equalTo("Posts"))
		assertThat(replyParameters["Posts.Count"], equalTo("2"))
		verifyFirstPost(replyParameters)
		verifySecondPost(replyParameters)
	}

	@Test
	fun `request with a maximum of 1 post returns only 1 post`() {
		setupPostsAndReplies()
		parameters += "MaxPosts" to "1"

		val replyParameters = command.execute(parameters).replyParameters
		assertThat(replyParameters["Message"], equalTo("Posts"))
		assertThat(replyParameters["Posts.Count"], equalTo("1"))
		verifyFirstPost(replyParameters)
	}

	@Test
	fun `request starting at the second post returns only 1 post`() {
		setupPostsAndReplies()
		parameters += "StartPost" to "1"

		val replyParameters = command.execute(parameters).replyParameters
		assertThat(replyParameters["Message"], equalTo("Posts"))
		assertThat(replyParameters["Posts.Count"], equalTo("1"))
		verifySecondPost(replyParameters, 0)
	}

	@Test
	fun `request skipping more posts than exist returns an empty list`() {
		setupPostsAndReplies()
		parameters += "StartPost" to "20"

		val replyParameters = command.execute(parameters).replyParameters
		assertThat(replyParameters["Message"], equalTo("Posts"))
		assertThat(replyParameters["Posts.Count"], equalTo("0"))
	}

}
