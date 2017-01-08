package net.pterodactylus.sone.fcp

import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [GetLocalSonesCommand].
 */
class GetLocalSonesCommandTest : SoneCommandTest() {

	override fun createCommand(core: Core) = GetLocalSonesCommand(core)

	@Test
	fun `command does not require write access`() {
		assertThat(command.requiresWriteAccess(), equalTo(false))
	}

	private fun createSone(id: String, name: String, firstName: String, lastName: String, time: Long) = mock<Sone>().apply {
		whenever(this.id).thenReturn(id)
		whenever(this.name).thenReturn(name)
		whenever(profile).thenReturn(Profile(this).apply {
			this.firstName = firstName
			this.lastName = lastName
		})
		whenever(this.time).thenReturn(time)
	}

	@Test
	fun `command returns local sones`() {
		val localSones = setOf(
				createSone("Id1", "Name1", "First1", "Last1", 1000L),
				createSone("Id2", "Name2", "First2", "Last2", 2000L)
		)
		whenever(core.localSones).thenReturn(localSones)
		val response = command.execute(null)
		val replyParameters = response.replyParameters
		assertThat(replyParameters["Message"], equalTo("ListLocalSones"))
		assertThat(replyParameters["LocalSones.Count"], equalTo("2"))
		assertThat(replyParameters["LocalSones.0.ID"], equalTo("Id1"))
		assertThat(replyParameters["LocalSones.0.Name"], equalTo("Name1"))
		assertThat(replyParameters["LocalSones.0.NiceName"], equalTo("First1 Last1"))
		assertThat(replyParameters["LocalSones.0.Time"], equalTo("1000"))
		assertThat(replyParameters["LocalSones.1.ID"], equalTo("Id2"))
		assertThat(replyParameters["LocalSones.1.Name"], equalTo("Name2"))
		assertThat(replyParameters["LocalSones.1.NiceName"], equalTo("First2 Last2"))
		assertThat(replyParameters["LocalSones.1.Time"], equalTo("2000"))
	}

}
