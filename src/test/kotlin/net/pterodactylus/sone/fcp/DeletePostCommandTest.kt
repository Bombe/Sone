package net.pterodactylus.sone.fcp

import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [DeletePostCommand].
 */
class DeletePostCommandTest : SoneCommandTest() {

	private val postFromRemoteSone = mock<Post>().apply { whenever(sone).thenReturn(remoteSone) }
	private val postFromLocalSone = mock<Post>().apply { whenever(sone).thenReturn(localSone) }
	override fun createCommand(core: Core) = DeletePostCommand(core)

	@Test
	fun `command requires write access`() {
		assertThat(command.requiresWriteAccess, equalTo(true))
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

	@Test
	fun `request with post from remote sone returns error response`() {
		parameters += "Post" to "RemotePostId"
		whenever(core.getPost("RemotePostId")).thenReturn(postFromRemoteSone)
		val response = command.execute(parameters)
		assertThat(response.replyParameters["Message"], equalTo("Error"))
		assertThat(response.replyParameters["ErrorCode"], equalTo("401"))
	}

	@Test
	fun `request with post from local sone deletes posts`() {
		parameters += "Post" to "LocalPostId"
		whenever(core.getPost("LocalPostId")).thenReturn(postFromLocalSone)
		val response = command.execute(parameters)
		assertThat(response.replyParameters["Message"], equalTo("PostDeleted"))
		verify(core).deletePost(postFromLocalSone)
	}

}
