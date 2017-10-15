package net.pterodactylus.sone.fcp

import freenet.support.SimpleFieldSet
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [GetPostFeedCommand].
 */
class GetPostFeedCommandTest : SoneCommandTest() {

	private val sone1 = createSone("Sone1", "Sone 1", "Sone", "#1", 1000)
	private val sone2 = createSone("Sone2", "Sone 2", "Sone", "#2", 2000)
	private val sone3 = createSone("Sone3", "Sone 3", "Sone", "#3", 3000)
	private val sone4 = createSone("Sone4", "Sone 4", "Sone", "#4", 4000)
	private val friend1 = createSone("Friend1", "Friend 1", "Friend", "#1", 5000)
	private val post1 = createPost("Post1", sone1, null, 1000, "Post 1")
	private val post1Reply1 = createReply("Post1Reply1", sone1, post1, 10000, "Post 1, Reply 1")
	private val post1Reply2 = createReply("Post1Reply2", sone2, post1, 20000, "Post 1, Reply 2")
	private val post2 = createPost("Post2", sone2, "Recipient 2", 2000, "Post 2")
	private val post2Reply1 = createReply("Post2Reply1", sone3, post2, 30000, "Post 2, Reply 1")
	private val post2Reply2 = createReply("Post2Reply2", sone4, post2, 40000, "Post 2, Reply 2")
	private val friendPost1 = createPost("FriendPost1", friend1, null, 1500, "Friend Post 1")
	private val directedPost = createPost("DirectedPost1", sone3, "ValidSoneId", 500, "Hey!")

	override fun createCommand(core: Core) = GetPostFeedCommand(core)

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

	@Test
	fun `request with valid remote Sone parameter results in fcp exception`() {
		requestWithValidRemoteSoneParameterResultsInFcpException()
	}

	private fun setupAllPostsAndReplies() {
		parameters += "Sone" to "ValidSoneId"
		whenever(localSone.id).thenReturn("ValidSoneId")
		whenever(core.getSone("ValidSoneId")).thenReturn(localSone)
		whenever(core.getSone("Friend1")).thenReturn(friend1)
		whenever(core.getLikes(post1)).thenReturn(setOf(sone3, sone4))
		whenever(core.getLikes(post1Reply1)).thenReturn(setOf(sone2, sone3))
		whenever(core.getLikes(post1Reply2)).thenReturn(setOf(sone3))
		whenever(core.getReplies("Post1")).thenReturn(listOf(post1Reply1, post1Reply2))
		whenever(core.getLikes(post2)).thenReturn(setOf(sone1, sone2))
		whenever(core.getLikes(post2Reply1)).thenReturn(setOf(sone4, sone1))
		whenever(core.getLikes(post2Reply2)).thenReturn(setOf(sone1, sone2, sone3))
		whenever(core.getReplies("Post2")).thenReturn(listOf(post2Reply1, post2Reply2))
		whenever(localSone.posts).thenReturn(listOf(post2, post1))
		whenever(core.getLikes(friendPost1)).thenReturn(setOf(sone1, friend1))
		whenever(friend1.posts).thenReturn(listOf(friendPost1))
		whenever(localSone.friends).thenReturn(setOf("Friend1", "Friend2"))
		whenever(core.getDirectedPosts("ValidSoneId")).thenReturn(setOf(directedPost))
		whenever(core.getLikes(directedPost)).thenReturn(setOf(sone2, sone4))
	}

	private fun verifyFirstPost(replyParameters: SimpleFieldSet) {
		assertThat(replyParameters.parsePost("Posts.0."), matchesPost(post2))
		assertThat(replyParameters["Posts.0.Replies.Count"], equalTo("2"))
		assertThat(replyParameters.parseReply("Posts.0.Replies.0."), matchesReply(post2Reply1))
		assertThat(replyParameters["Posts.0.Replies.0.Likes.Count"], equalTo("2"))
		assertThat((0..1).map { replyParameters["Posts.0.Replies.0.Likes.$it.ID"] }, containsInAnyOrder("Sone1", "Sone4"))
		assertThat(replyParameters.parseReply("Posts.0.Replies.1."), matchesReply(post2Reply2))
		assertThat(replyParameters["Posts.0.Replies.1.Likes.Count"], equalTo("3"))
		assertThat((0..2).map { replyParameters["Posts.0.Replies.1.Likes.$it.ID"] }, containsInAnyOrder("Sone1", "Sone2", "Sone3"))
		assertThat(replyParameters["Posts.0.Likes.Count"], equalTo("2"))
		assertThat((0..1).map { replyParameters["Posts.0.Likes.$it.ID"] }, containsInAnyOrder("Sone1", "Sone2"))
	}

	private fun verifySecondPost(replyParameters: SimpleFieldSet, index: Int = 1) {
		assertThat(replyParameters.parsePost("Posts.$index."), matchesPost(friendPost1))
		assertThat(replyParameters["Posts.$index.Replies.Count"], equalTo("0"))
		assertThat(replyParameters["Posts.$index.Likes.Count"], equalTo("2"))
		assertThat((0..1).map { replyParameters["Posts.$index.Likes.$it.ID"] }, containsInAnyOrder("Sone1", "Friend1"))
	}

	private fun verifyThirdPost(replyParameters: SimpleFieldSet, index: Int = 2) {
		assertThat(replyParameters.parsePost("Posts.$index."), matchesPost(post1))
		assertThat(replyParameters.parseReply("Posts.$index.Replies.0."), matchesReply(post1Reply1))
		assertThat(replyParameters.parseReply("Posts.$index.Replies.1."), matchesReply(post1Reply2))
		assertThat(replyParameters["Posts.$index.Replies.0.Likes.Count"], equalTo("2"))
		assertThat((0..1).map { replyParameters["Posts.$index.Replies.0.Likes.$it.ID"] }, containsInAnyOrder("Sone2", "Sone3"))
		assertThat(replyParameters["Posts.$index.Replies.1.Likes.Count"], equalTo("1"))
		assertThat(replyParameters["Posts.$index.Replies.1.Likes.0.ID"], equalTo("Sone3"))
		assertThat(replyParameters["Posts.$index.Likes.Count"], equalTo("2"))
		assertThat((0..1).map { replyParameters["Posts.$index.Likes.$it.ID"] }, containsInAnyOrder("Sone3", "Sone4"))
	}

	private fun verifyFourthPost(replyParameters: SimpleFieldSet, index: Int = 3) {
		assertThat(replyParameters.parsePost("Posts.$index."), matchesPost(directedPost))
		assertThat(replyParameters["Posts.$index.Replies.Count"], equalTo("0"))
		assertThat(replyParameters["Posts.$index.Likes.Count"], equalTo("2"))
		assertThat((0..1).map { replyParameters["Posts.$index.Likes.$it.ID"] }, containsInAnyOrder("Sone2", "Sone4"))
	}

	@Test
	fun `request with valid local Sone parameter results in the post feed with all required posts`() {
		setupAllPostsAndReplies()

		val replyParameters = command.execute(parameters).replyParameters

		assertThat(replyParameters["Message"], equalTo("PostFeed"))
		assertThat(replyParameters["Posts.Count"], equalTo("4"))

		verifyFirstPost(replyParameters)
		verifySecondPost(replyParameters)
		verifyThirdPost(replyParameters)
		verifyFourthPost(replyParameters)
	}

	@Test
	fun `request with larger start than number of posts returns empty feed`() {
		setupAllPostsAndReplies()
		parameters += "StartPost" to "20"
		val replyParameters = command.execute(parameters).replyParameters
		assertThat(replyParameters["Message"], equalTo("PostFeed"))
		assertThat(replyParameters["Posts.Count"], equalTo("0"))
	}

	@Test
	fun `request with max posts of 2 returns the first two posts`() {
		setupAllPostsAndReplies()
		parameters += "MaxPosts" to "2"
		val replyParameters = command.execute(parameters).replyParameters
		assertThat(replyParameters["Message"], equalTo("PostFeed"))
		assertThat(replyParameters["Posts.Count"], equalTo("2"))

		verifyFirstPost(replyParameters)
		verifySecondPost(replyParameters)
	}

	@Test
	fun `request with max posts of 2 and start post of 1 returns the center two posts`() {
		setupAllPostsAndReplies()
		parameters += "StartPost" to "1"
		parameters += "MaxPosts" to "2"
		val replyParameters = command.execute(parameters).replyParameters
		assertThat(replyParameters["Message"], equalTo("PostFeed"))
		assertThat(replyParameters["Posts.Count"], equalTo("2"))

		verifySecondPost(replyParameters, 0)
		verifyThirdPost(replyParameters, 1)
	}

	@Test
	fun `request with max posts of 2 and start post of 3 returns the last post`() {
		setupAllPostsAndReplies()
		parameters += "StartPost" to "3"
		parameters += "MaxPosts" to "2"
		val replyParameters = command.execute(parameters).replyParameters
		assertThat(replyParameters["Message"], equalTo("PostFeed"))
		assertThat(replyParameters["Posts.Count"], equalTo("1"))

		verifyFourthPost(replyParameters, 0)
	}

}
