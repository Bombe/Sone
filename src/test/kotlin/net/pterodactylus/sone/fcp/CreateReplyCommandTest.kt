package net.pterodactylus.sone.fcp

import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*

/**
 * Unit test for [CreateReplyCommand].
 */
class CreateReplyCommandTest : SoneCommandTest() {

	private val post = mock<Post>()

	override fun createCommand(core: Core) = CreateReplyCommand(core)

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

	private fun addValidLocalSoneParameter() {
		parameters += "Sone" to "LocalSoneId"
		whenever(core.getSone("LocalSoneId")).thenReturn(localSone)
	}

	@Test
	fun `request without post parameter results in fcp exception`() {
		addValidLocalSoneParameter()
		executeCommandAndExpectFcpException()
	}

	@Test
	fun `request with invalid post parameter results in fcp exception`() {
		addValidLocalSoneParameter()
		parameters += "Post" to "InvalidPostId"
		executeCommandAndExpectFcpException()
	}

	private fun addValidPostParameter() {
		parameters += "Post" to "ValidPostId"
		whenever(core.getPost("ValidPostId")).thenReturn(post)
	}

	@Test
	fun `request without text results in fcp exception`() {
		addValidLocalSoneParameter()
		addValidPostParameter()
		executeCommandAndExpectFcpException()
	}

	@Test
	fun `complete request creates reply`() {
		addValidLocalSoneParameter()
		addValidPostParameter()
		parameters += "Text" to "Test"
		val postReply = mock<PostReply>().apply { whenever(id).thenReturn("ReplyId") }
		whenever(core.createReply(localSone, post, "Test")).thenReturn(postReply)
		val response = command.execute(parameters)
		assertThat(response.replyParameters["Message"], equalTo("ReplyCreated"))
		assertThat(response.replyParameters["Reply"], equalTo("ReplyId"))
	}

}
