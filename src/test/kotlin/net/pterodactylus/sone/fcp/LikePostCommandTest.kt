package net.pterodactylus.sone.fcp

import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.freenet.fcp.FcpException
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.utils.asOptional
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [LikePostCommand].
 */
class LikePostCommandTest : SoneCommandTest() {

	private val post = createPost("PostId", mock(), null, 1000, "Text")

	override fun createCommand(core: Core) = LikePostCommand(core)

	@Before
	fun setupPostAndSones() {
		whenever(core.getPost("PostId")).thenReturn(post)
		whenever(core.getSone("RemoteSoneId")).thenReturn(remoteSone)
		whenever(core.getSone("LocalSoneId")).thenReturn(localSone)
	}

	@Test
	fun `command requires write access`() {
		assertThat(command.requiresWriteAccess, equalTo(true))
	}

	@Test
	fun `request without parameters results in FCP exception`() {
		requestWithoutAnyParameterResultsInFcpException()
	}

	@Test
	fun `request with invalid post id results in FCP exception`() {
		parameters += "Post" to "InvalidPostId"
		expectedException.expect(FcpException::class.java)
		command.execute(parameters)
	}

	@Test
	fun `request with missing local sone results in FCP exception`() {
		parameters += "Post" to "PostId"
		expectedException.expect(FcpException::class.java)
		command.execute(parameters)
	}

	@Test
	fun `request with invalid sone results in FCP exception`() {
		parameters += "Post" to "PostId"
		parameters += "Sone" to "InvalidSoneId"
		expectedException.expect(FcpException::class.java)
		command.execute(parameters)
	}

	@Test
	fun `request with valid remote sone results in FCP exception`() {
		parameters += "Post" to "PostId"
		parameters += "Sone" to "RemoteSoneId"
		expectedException.expect(FcpException::class.java)
		command.execute(parameters)
	}

	@Test
	fun `request with valid parameters adds post to liked posts for sone`() {
		whenever(core.getLikes(post)).thenReturn(setOf(mock(), mock(), mock()))
		parameters += "Post" to "PostId"
		parameters += "Sone" to "LocalSoneId"
		val replyParameters = command.execute(parameters).replyParameters
		assertThat(replyParameters["Message"], equalTo("PostLiked"))
		assertThat(replyParameters["LikeCount"], equalTo("3"))
		verify(localSone).addLikedPostId("PostId")
	}

}
