package net.pterodactylus.sone.fcp

import com.google.common.base.Optional.of
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [DeletePostCommand].
 */
class DeletePostCommandTest : SoneCommandTest() {

	private val postFromRemoteSone = mock<Post>().apply { whenever(sone).thenReturn(remoteSone) }
	private val postFromLocalSone = mock<Post>().apply { whenever(sone).thenReturn(localSone) }
	override fun createCommand(core: Core) = DeletePostCommand(core)

	@Test
	fun `command requires write access`() {
		assertThat(command.requiresWriteAccess(), equalTo(true))
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

	@Test
	fun `request with post from remote sone returns error response`() {
		parameters.putSingle("Post", "RemotePostId")
		whenever(core.getPost("RemotePostId")).thenReturn(of(postFromRemoteSone))
		val response = command.execute(parameters, null, null)
		assertThat(response.replyParameters["Message"], equalTo("Error"))
		assertThat(response.replyParameters["ErrorCode"], equalTo("401"))
	}

	@Test
	fun `request with post from local sone deletes posts`() {
		parameters.putSingle("Post", "LocalPostId")
		whenever(core.getPost("LocalPostId")).thenReturn(of(postFromLocalSone))
		val response = command.execute(parameters, null, null)
		assertThat(response.replyParameters["Message"], equalTo("PostDeleted"))
		verify(core).deletePost(postFromLocalSone)
	}

}
