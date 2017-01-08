package net.pterodactylus.sone.fcp

import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.ALWAYS
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.NO
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.WRITING
import net.pterodactylus.sone.fcp.event.FcpInterfaceActivatedEvent
import net.pterodactylus.sone.fcp.event.FcpInterfaceDeactivatedEvent
import net.pterodactylus.sone.fcp.event.FullAccessRequiredChanged
import net.pterodactylus.sone.test.mock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [FcpInterface] and its subclasses.
 */
class FcpInterfaceTest {

	private val core = mock<Core>()
	private val fcpInterface = FcpInterface(core)

	@Test
	fun `fcp interface can be activated`() {
		fcpInterface.fcpInterfaceActivated(FcpInterfaceActivatedEvent())
		assertThat(fcpInterface.isActive, equalTo(true))
	}

	@Test
	fun `fcp interface can be deactivated`() {
		fcpInterface.fcpInterfaceDeactivated(FcpInterfaceDeactivatedEvent())
		assertThat(fcpInterface.isActive, equalTo(false))
	}

	private fun setAndVerifyAccessRequired(fullAccessRequired: FullAccessRequired) {
		fcpInterface.fullAccessRequiredChanged(FullAccessRequiredChanged(fullAccessRequired))
		assertThat(fcpInterface.fullAccessRequired, equalTo(fullAccessRequired))
	}

	@Test
	fun `set full access required can set access to no`() {
		setAndVerifyAccessRequired(NO)
	}

	@Test
	fun `set full access required can set access to writing`() {
		setAndVerifyAccessRequired(WRITING)
	}

	@Test
	fun `set full access required can set access to always`() {
		setAndVerifyAccessRequired(ALWAYS)
	}

}
