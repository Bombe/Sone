package net.pterodactylus.sone.fcp

import com.google.common.base.Optional.absent
import com.google.common.base.Optional.of
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test

/**
 * Unit test for [CreatePostCommand].
 */
class CreatePostCommandTest : SoneCommandTest() {

	override fun createCommand(core: Core) = CreatePostCommand(core)

	@Test
	fun `command requires write access`() {
		assertThat(command.requiresWriteAccess, equalTo(true))
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

	@Test
	fun `request without text results in fcp exception`() {
		parameters += "Sone" to "LocalSoneId"
		whenever(core.getSone("LocalSoneId")).thenReturn(localSone)
		executeCommandAndExpectFcpException()
	}

	@Test
	fun `request with text creates post`() {
		parameters += "Sone" to "LocalSoneId"
		parameters += "Text" to "Test"
		whenever(core.getSone("LocalSoneId")).thenReturn(localSone)
		val post = mock<Post>().apply { whenever(id).thenReturn("PostId") }
		whenever(core.createPost(localSone, absent(), "Test")).thenReturn(post)
		val response = command.execute(parameters)
		assertThat(response.replyParameters.get("Message"), equalTo("PostCreated"))
		assertThat(response.replyParameters.get("Post"), equalTo("PostId"))
	}

	@Test
	fun `request with invalid recipient results in fcp exception`() {
		parameters += "Sone" to "LocalSoneId"
		parameters += "Text" to "Test"
		parameters += "Recipient" to "InvalidSoneId"
		whenever(core.getSone("LocalSoneId")).thenReturn(localSone)
		executeCommandAndExpectFcpException()
	}

	@Test
	fun `request with recipient the same as the sender returns an error response`() {
		parameters += "Sone" to "LocalSoneId"
		parameters += "Text" to "Test"
		parameters += "Recipient" to "LocalSoneId"
		whenever(core.getSone("LocalSoneId")).thenReturn(localSone)
		val response = command.execute(parameters)
		assertThat(response.replyParameters["Message"], equalTo("Error"))
		assertThat(response.replyParameters["ErrorMessage"], notNullValue())
	}

	@Test
	fun `request with text and recipient creates post`() {
		parameters += "Sone" to "LocalSoneId"
		parameters += "Text" to "Test"
		parameters += "Recipient" to "RemoteSoneId"
		whenever(core.getSone("LocalSoneId")).thenReturn(localSone)
		whenever(core.getSone("RemoteSoneId")).thenReturn(remoteSone)
		val post = mock<Post>().apply { whenever(id).thenReturn("PostId") }
		whenever(core.createPost(localSone, of(remoteSone), "Test")).thenReturn(post)
		val response = command.execute(parameters)
		assertThat(response.replyParameters.get("Message"), equalTo("PostCreated"))
		assertThat(response.replyParameters.get("Post"), equalTo("PostId"))
	}

}
