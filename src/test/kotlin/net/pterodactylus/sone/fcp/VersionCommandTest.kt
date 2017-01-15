package net.pterodactylus.sone.fcp

import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.main.SonePlugin
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [VersionCommand].
 */
class VersionCommandTest : SoneCommandTest() {

	override fun createCommand(core: Core) = VersionCommand(core)

	@Test
	fun `command does not require write access`() {
		assertThat(command.requiresWriteAccess(), equalTo(false))
	}

	@Test
	fun `command replies with the correct version information`() {
		val replyParameters = command.execute(parameters).replyParameters
		assertThat(replyParameters["Message"], equalTo("Version"))
		assertThat(replyParameters["Version"], equalTo(SonePlugin.getPluginVersion().toString()))
		assertThat(replyParameters["ProtocolVersion"], equalTo("1"))
	}

}
