package net.pterodactylus.sone.fcp

import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.freenet.fcp.FcpException
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test

/**
 * Unit test for [GetSoneCommand].
 */
class GetSoneCommandTest : SoneCommandTest() {

	private val sone = createSone("SoneId", "Sone", "Sone", "#1", 1000).apply {
		profile.addField("Test").value = "true"
		profile.addField("More Test").value = "also true"
	}

	override fun createCommand(core: Core) = GetSoneCommand(core)

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
	fun `request with valid Sone parameter results in response with Sone information`() {
		whenever(core.getSone("SoneId")).thenReturn(sone)
		parameters += "Sone" to "SoneId"
		val replyParameters = command.execute(parameters).replyParameters
		assertThat(replyParameters["Message"], equalTo("Sone"))
		assertThat(replyParameters.parseSone("Sone."), matchesSone(sone))
		assertThat(replyParameters["Sone.Followed"], nullValue())
	}

	@Test
	fun `request with local sone parameter results in followed being true for friend sone`() {
		whenever(core.getSone("SoneId")).thenReturn(sone)
		whenever(core.getSone("LocalSone")).thenReturn(localSone)
		whenever(localSone.id).thenReturn("LocalSone")
		whenever(localSone.hasFriend("SoneId")).thenReturn(true)
		parameters += "Sone" to "SoneId"
		parameters += "LocalSone" to "LocalSone"
		val replyParameters = command.execute(parameters).replyParameters
		assertThat(replyParameters["Message"], equalTo("Sone"))
		assertThat(replyParameters.parseSone("Sone."), matchesSone(sone))
		assertThat(replyParameters["Sone.Followed"], equalTo("true"))
	}
	
	@Test
	fun `request with local sone parameter results in followed being false for non-friend sone`() {
		whenever(core.getSone("SoneId")).thenReturn(sone)
		whenever(core.getSone("LocalSone")).thenReturn(localSone)
		whenever(localSone.id).thenReturn("LocalSone")
		parameters += "Sone" to "SoneId"
		parameters += "LocalSone" to "LocalSone"
		val replyParameters = command.execute(parameters).replyParameters
		assertThat(replyParameters["Message"], equalTo("Sone"))
		assertThat(replyParameters.parseSone("Sone."), matchesSone(sone))
		assertThat(replyParameters["Sone.Followed"], equalTo("false"))
	}

	@Test
	fun `request with remote sone as local sone parameter results in fcp exception`() {
		whenever(core.getSone("SoneId")).thenReturn(sone)
		whenever(core.getSone("RemoteSone")).thenReturn(remoteSone)
		whenever(localSone.id).thenReturn("RemoteSone")
		parameters += "Sone" to "SoneId"
		parameters += "LocalSone" to "RemoteSone"
		expectedException.expect(FcpException::class.java)
		command.execute(parameters)
	}

}
