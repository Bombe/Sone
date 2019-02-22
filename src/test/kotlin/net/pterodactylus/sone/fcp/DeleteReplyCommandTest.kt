package net.pterodactylus.sone.fcp

import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [DeleteReplyCommand].
 */
class DeleteReplyCommandTest : SoneCommandTest() {

	private val remotePostReply = mock<PostReply>().apply { whenever(sone).thenReturn(remoteSone) }
	private val localPostReply = mock<PostReply>().apply { whenever(sone).thenReturn(localSone) }

	override fun createCommand(core: Core) = DeleteReplyCommand(core)

	@Test
	fun `command requires write access`() {
		assertThat(command.requiresWriteAccess, equalTo(true))
	}

	@Test
	fun `request without any parameters results in fcp exception`() {
		requestWithoutAnyParameterResultsInFcpException()
	}

	@Test
	fun `request with invalid post reply parameter results in fcp exception`() {
		parameters += "Reply" to "InvalidReplyId"
		executeCommandAndExpectFcpException()
	}

	@Test
	fun `request with remote post reply parameter results in error response`() {
	    parameters += "Reply" to "RemoteReplyId"
		whenever(core.getPostReply("RemoteReplyId")).thenReturn(remotePostReply)
		val response = command.execute(parameters)
		assertThat(response.replyParameters["Message"], equalTo("Error"))
		assertThat(response.replyParameters["ErrorCode"], equalTo("401"))
	}

	@Test
	fun `request with local post reply parameter deletes reply`() {
	    parameters += "Reply" to "RemoteReplyId"
		whenever(core.getPostReply("RemoteReplyId")).thenReturn(localPostReply)
		val response = command.execute(parameters)
		assertThat(response.replyParameters["Message"], equalTo("ReplyDeleted"))
		verify(core).deleteReply(localPostReply)
	}

}
