@file:Suppress("DEPRECATION")

package net.pterodactylus.sone.fcp

import freenet.pluginmanager.PluginNotFoundException
import freenet.pluginmanager.PluginReplySender
import freenet.support.SimpleFieldSet
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.ALWAYS
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.NO
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.WRITING
import net.pterodactylus.sone.fcp.event.FcpInterfaceActivatedEvent
import net.pterodactylus.sone.fcp.event.FcpInterfaceDeactivatedEvent
import net.pterodactylus.sone.fcp.event.FullAccessRequiredChanged
import net.pterodactylus.sone.freenet.fcp.Command.AccessType.FULL_FCP
import net.pterodactylus.sone.freenet.fcp.Command.AccessType.RESTRICTED_FCP
import net.pterodactylus.sone.main.SonePlugin
import net.pterodactylus.sone.test.capture
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.verify

/**
 * Unit test for [FcpInterface] and its subclasses.
 */
class FcpInterfaceTest {

	private val core = mock<Core>()
	private val fcpInterface = FcpInterface(core)
	private val pluginReplySender = mock<PluginReplySender>()
	private val parameters = SimpleFieldSet(true)
	private val replyParameters = capture<SimpleFieldSet>()

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

	@Test
	fun `sending command to inactive fcp interface results in 400 error reply`() {
		fcpInterface.fcpInterfaceDeactivated(FcpInterfaceDeactivatedEvent())
		fcpInterface.handle(pluginReplySender, parameters, null, 0)
		verify(pluginReplySender).send(replyParameters.capture())
		assertThat(replyParameters.value["Message"], equalTo("Error"))
		assertThat(replyParameters.value["ErrorCode"], equalTo("503"))
	}

	@Test
	fun `exception while sending reply does not result in exception`() {
		fcpInterface.fcpInterfaceDeactivated(FcpInterfaceDeactivatedEvent())
		whenever(pluginReplySender.send(ArgumentMatchers.any())).thenThrow(PluginNotFoundException::class.java)
		fcpInterface.handle(pluginReplySender, parameters, null, 0)
	}

	@Test
	fun `sending command over restricted fcp connection results in 401 error reply`() {
		fcpInterface.fcpInterfaceActivated(FcpInterfaceActivatedEvent())
		fcpInterface.handle(pluginReplySender, parameters, null, RESTRICTED_FCP.ordinal)
		verify(pluginReplySender).send(replyParameters.capture())
		assertThat(replyParameters.value["Message"], equalTo("Error"))
		assertThat(replyParameters.value["ErrorCode"], equalTo("401"))
	}

	@Test
	fun `sending unknown command over full access connection results in 404 error reply`() {
		fcpInterface.fcpInterfaceActivated(FcpInterfaceActivatedEvent())
		fcpInterface.handle(pluginReplySender, parameters, null, FULL_FCP.ordinal)
		verify(pluginReplySender).send(replyParameters.capture())
		assertThat(replyParameters.value["Message"], equalTo("Error"))
		assertThat(replyParameters.value["ErrorCode"], equalTo("404"))
	}

	@Test
	fun `sending version command without identifier results in 400 error code`() {
		fcpInterface.fcpInterfaceActivated(FcpInterfaceActivatedEvent())
		parameters.putSingle("Message", "Version")
		fcpInterface.handle(pluginReplySender, parameters, null, FULL_FCP.ordinal)
		verify(pluginReplySender).send(replyParameters.capture())
		assertThat(replyParameters.value["Message"], equalTo("Error"))
		assertThat(replyParameters.value["ErrorCode"], equalTo("400"))
	}

	@Test
	fun `sending version command with empty identifier results in 400 error code`() {
		fcpInterface.fcpInterfaceActivated(FcpInterfaceActivatedEvent())
		parameters.putSingle("Message", "Version")
		parameters.putSingle("Identifier", "")
		fcpInterface.handle(pluginReplySender, parameters, null, FULL_FCP.ordinal)
		verify(pluginReplySender).send(replyParameters.capture())
		assertThat(replyParameters.value["Message"], equalTo("Error"))
		assertThat(replyParameters.value["ErrorCode"], equalTo("400"))
	}

	@Test
	fun `sending version command with identifier results in version reply`() {
		fcpInterface.fcpInterfaceActivated(FcpInterfaceActivatedEvent())
		parameters.putSingle("Message", "Version")
		parameters.putSingle("Identifier", "Test")
		fcpInterface.handle(pluginReplySender, parameters, null, FULL_FCP.ordinal)
		verify(pluginReplySender).send(replyParameters.capture())
		assertThat(replyParameters.value["Message"], equalTo("Version"))
		assertThat(replyParameters.value["Version"], equalTo(SonePlugin.getPluginVersion()))
		assertThat(replyParameters.value["ProtocolVersion"], equalTo("1"))
	}

}
