package net.pterodactylus.sone.fcp

import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [GetLocalSonesCommand].
 */
class GetLocalSonesCommandTest : SoneCommandTest() {

	private val sone1 = createSone("Id1", "Name1", "First1", "Last1", 1000L)
	private val sone2 = createSone("Id2", "Name2", "First2", "Last2", 2000L)

	override fun createCommand(core: Core) = GetLocalSonesCommand(core)

	@Test
	fun `command does not require write access`() {
		assertThat(command.requiresWriteAccess(), equalTo(false))
	}

	@Test
	fun `command returns local sones`() {
		val localSones = setOf(sone1, sone2)
		whenever(core.localSones).thenReturn(localSones)
		val response = command.execute(null)
		val replyParameters = response.replyParameters
		assertThat(replyParameters["Message"], equalTo("ListLocalSones"))
		assertThat(replyParameters["LocalSones.Count"], equalTo("2"))
		assertThat(replyParameters.parseSone("LocalSones.0."), matchesSone(sone1))
		assertThat(replyParameters.parseSone("LocalSones.1."), matchesSone(sone2))
	}

}
