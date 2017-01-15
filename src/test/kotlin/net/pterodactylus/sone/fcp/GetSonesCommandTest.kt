package net.pterodactylus.sone.fcp

import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test

/**
 * Unit test for [GetSonesCommand].
 */
class GetSonesCommandTest : SoneCommandTest() {

	private val sone1 = createSone("SoneId1", "Sone1", "Sone", "#1", 1000)
	private val sone2 = createSone("SoneId2", "Sone2", "Sone", "#2", 2000)
	private val sone3 = createSone("SoneId3", "Sone3", "Sone", "#3", 3000)

	override fun createCommand(core: Core) = GetSonesCommand(core)

	@Before
	fun setupSones() {
		whenever(core.sones).thenReturn(setOf(sone2, sone3, sone1))
	}

	@Test
	fun `command does not require write access`() {
		assertThat(command.requiresWriteAccess(), equalTo(false))
	}

	@Test
	fun `request without parameters lists all sones`() {
	    val replyParameters = command.execute(parameters).replyParameters

		assertThat(replyParameters["Message"], equalTo("Sones"))
		assertThat(replyParameters["Sones.Count"], equalTo("3"))
		assertThat(replyParameters.parseSone("Sones.0."), matchesSone(sone1))
		assertThat(replyParameters.parseSone("Sones.1."), matchesSone(sone2))
		assertThat(replyParameters.parseSone("Sones.2."), matchesSone(sone3))
	}

	@Test
	fun `skipping the first sone lists the last two sones`() {
		parameters += "StartSone" to "1"
	    val replyParameters = command.execute(parameters).replyParameters

		assertThat(replyParameters["Message"], equalTo("Sones"))
		assertThat(replyParameters["Sones.Count"], equalTo("2"))
		assertThat(replyParameters.parseSone("Sones.0."), matchesSone(sone2))
		assertThat(replyParameters.parseSone("Sones.1."), matchesSone(sone3))
	}

	@Test
	fun `requesting only two sones lists the first two sones`() {
		parameters += "MaxSones" to "2"
	    val replyParameters = command.execute(parameters).replyParameters

		assertThat(replyParameters["Message"], equalTo("Sones"))
		assertThat(replyParameters["Sones.Count"], equalTo("2"))
		assertThat(replyParameters.parseSone("Sones.0."), matchesSone(sone1))
		assertThat(replyParameters.parseSone("Sones.1."), matchesSone(sone2))
	}

	@Test
	fun `skipping more sones than there are lists no sones`() {
		parameters += "StartSone" to "20"
	    val replyParameters = command.execute(parameters).replyParameters

		assertThat(replyParameters["Message"], equalTo("Sones"))
		assertThat(replyParameters["Sones.Count"], equalTo("0"))
	}

}
