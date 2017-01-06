package net.pterodactylus.sone.fcp

import com.google.common.base.Optional.absent
import com.google.common.base.Optional.of
import freenet.support.SimpleFieldSet
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.freenet.fcp.FcpException
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.ArgumentMatchers.anyString

/**
 * Unit test for [CreatePostCommand].
 */
class CreatePostCommandTest {

	@Rule @JvmField val expectedException = ExpectedException.none()
	private val core = mock<Core>()
	private val command = CreatePostCommand(core)

	private val parameters = SimpleFieldSet(true)
	private val localSone = mock<Sone>().apply {
		whenever(isLocal).thenReturn(true)
	}
	private val remoteSone = mock<Sone>()

	@Before
	fun setupCore() {
		whenever(core.getSone(anyString())).thenReturn(absent())
	}

	@Test
	fun `command requires write access`() {
	    assertThat(command.requiresWriteAccess(), equalTo(true))
	}

	@Test
	fun `request without any parameters results in fcp exception`() {
		expectedException.expect(FcpException::class.java)
		command.execute(parameters, null, null)
	}

	@Test
	fun `request with empty sone parameter results in fcp exception`() {
		parameters.putSingle("Sone", null)
		expectedException.expect(FcpException::class.java)
		command.execute(parameters, null, null)
	}

	@Test
	fun `request with invalid Sone parameter results in fcp exception`() {
		parameters.putSingle("Sone", "InvalidSoneId")
		expectedException.expect(FcpException::class.java)
		command.execute(parameters, null, null)
	}

	@Test
	fun `request with valid remote Sone parameter results in fcp exception`() {
		parameters.putSingle("Sone", "RemoteSoneId")
		whenever(core.getSone("RemoteSoneId")).thenReturn(of(remoteSone))
		expectedException.expect(FcpException::class.java)
		command.execute(parameters, null, null)
	}

	@Test
	fun `request without text results in fcp exception`() {
		parameters.putSingle("Sone", "LocalSoneId")
		whenever(core.getSone("LocalSoneId")).thenReturn(of(localSone))
		expectedException.expect(FcpException::class.java)
		command.execute(parameters, null, null)
	}

	@Test
	fun `request with text creates post`() {
		parameters.putSingle("Sone", "LocalSoneId")
		parameters.putSingle("Text", "Test")
		whenever(core.getSone("LocalSoneId")).thenReturn(of(localSone))
		val post = mock<Post>().apply { whenever(id).thenReturn("PostId") }
		whenever(core.createPost(localSone, absent(), "Test")).thenReturn(post)
		val response = command.execute(parameters, null, null)
		assertThat(response.replyParameters.get("Message"), equalTo("PostCreated"))
		assertThat(response.replyParameters.get("Post"), equalTo("PostId"))
	}

	@Test
	fun `request with invalid recipient results in fcp exception`() {
		parameters.putSingle("Sone", "LocalSoneId")
		parameters.putSingle("Text", "Test")
		parameters.putSingle("Recipient", "InvalidSoneId")
		whenever(core.getSone("LocalSoneId")).thenReturn(of(localSone))
		expectedException.expect(FcpException::class.java)
		command.execute(parameters, null, null)
	}

	@Test
	fun `request with recipient the same as the sender returns an error response`() {
		parameters.putSingle("Sone", "LocalSoneId")
		parameters.putSingle("Text", "Test")
		parameters.putSingle("Recipient", "LocalSoneId")
		whenever(core.getSone("LocalSoneId")).thenReturn(of(localSone))
		val response = command.execute(parameters, null, null)
		assertThat(response.replyParameters["Message"], equalTo("Error"))
		assertThat(response.replyParameters["ErrorMessage"], notNullValue())
	}

	@Test
	fun `request with text and recipient creates post`() {
		parameters.putSingle("Sone", "LocalSoneId")
		parameters.putSingle("Text", "Test")
		parameters.putSingle("Recipient", "RemoteSoneId")
		whenever(core.getSone("LocalSoneId")).thenReturn(of(localSone))
		whenever(core.getSone("RemoteSoneId")).thenReturn(of(remoteSone))
		val post = mock<Post>().apply { whenever(id).thenReturn("PostId") }
		whenever(core.createPost(localSone, of(remoteSone), "Test")).thenReturn(post)
		val response = command.execute(parameters, null, null)
		assertThat(response.replyParameters.get("Message"), equalTo("PostCreated"))
		assertThat(response.replyParameters.get("Post"), equalTo("PostId"))
	}

}
