package net.pterodactylus.sone.fcp

import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.freenet.fcp.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [LikeReplyCommand].
 */
class LikeReplyCommandTest : SoneCommandTest() {

	private val reply = createReply("ReplyId", mock(), mock(), 1000, "Text")

	override fun createCommand(core: Core) = LikeReplyCommand(core)

	@Before
	fun setupRepliesAndSones() {
		whenever(core.getPostReply("ReplyId")).thenReturn(reply)
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
	fun `request with invalid reply results in FCP exception`() {
		parameters += "Reply" to "InvalidReplyId"
		expectedException.expect(FcpException::class.java)
		command.execute(parameters)
	}

	@Test
	fun `request without sone results in FCP exception`() {
		parameters += "Reply" to "ReplyId"
		expectedException.expect(FcpException::class.java)
		command.execute(parameters)
	}

	@Test
	fun `request with invalid sone results in FCP exception`() {
		parameters += "Reply" to "ReplyId"
		parameters += "Sone" to "InvalidSoneId"
		expectedException.expect(FcpException::class.java)
		command.execute(parameters)
	}

	@Test
	fun `request with remote sone results in FCP exception`() {
		parameters += "Reply" to "ReplyId"
		parameters += "Sone" to "RemoteSoneId"
		expectedException.expect(FcpException::class.java)
		command.execute(parameters)
	}

	@Test
	fun `request with local sone adds reply id to sone`() {
		whenever(core.getLikes(reply)).thenReturn(setOf(mock(), mock(), mock()))
		parameters += "Reply" to "ReplyId"
		parameters += "Sone" to "LocalSoneId"
		val replyParameters = command.execute(parameters).replyParameters
		assertThat(replyParameters["Message"], equalTo("ReplyLiked"))
		assertThat(replyParameters["LikeCount"], equalTo("3"))
		verify(localSone).addLikedReplyId("ReplyId")
	}

}
