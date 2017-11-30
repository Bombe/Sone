package net.pterodactylus.sone.fcp

import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [UnlockSoneCommand].
 */
class UnlockSoneCommandTest : SoneCommandTest() {

	override fun createCommand(core: Core) = UnlockSoneCommand(core)

	@Before
	fun setupSones() {
		whenever(core.getSone("RemoteSoneId")).thenReturn(remoteSone)
		whenever(core.getSone("LocalSoneId")).thenReturn(localSone)
		whenever(localSone.id).thenReturn("LocalSoneId")
	}

	@Test
	fun `command requires write access`() {
		assertThat(command.requiresWriteAccess(), equalTo(true))
	}

	@Test
	fun `request without any parameters results in FCP exception`() {
		requestWithoutAnyParameterResultsInFcpException()
	}

	@Test
	fun `request with invalid sone parameter results in FCP exception`() {
		requestWithInvalidSoneParameterResultsInFcpException()
	}

	@Test
	fun `request with valid remote sone parameter results in FCP exception`() {
		requestWithValidRemoteSoneParameterResultsInFcpException()
	}

	@Test
	fun `request with local sone parameter unlocks the sone`() {
		parameters += "Sone" to "LocalSoneId"
		val replyParameters = command.execute(parameters).replyParameters
		assertThat(replyParameters["Message"], equalTo("SoneUnlocked"))
		assertThat(replyParameters["Sone"], equalTo("LocalSoneId"))
		verify(core).unlockSone(localSone)
	}

}
