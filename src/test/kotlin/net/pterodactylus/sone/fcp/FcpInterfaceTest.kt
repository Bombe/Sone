@file:Suppress("DEPRECATION")

package net.pterodactylus.sone.fcp

import com.google.inject.Guice
import freenet.pluginmanager.PluginNotFoundException
import freenet.pluginmanager.PluginReplySender
import freenet.support.SimpleFieldSet
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.fcp.FcpInterface.AccessAuthorizer
import net.pterodactylus.sone.fcp.FcpInterface.CommandSupplier
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.ALWAYS
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.NO
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.WRITING
import net.pterodactylus.sone.fcp.event.FcpInterfaceActivatedEvent
import net.pterodactylus.sone.fcp.event.FcpInterfaceDeactivatedEvent
import net.pterodactylus.sone.fcp.event.FullAccessRequiredChanged
import net.pterodactylus.sone.freenet.fcp.Command.AccessType
import net.pterodactylus.sone.freenet.fcp.Command.AccessType.FULL_FCP
import net.pterodactylus.sone.freenet.fcp.Command.AccessType.RESTRICTED_FCP
import net.pterodactylus.sone.freenet.fcp.Command.Response
import net.pterodactylus.sone.test.capture
import net.pterodactylus.sone.test.isProvidedBy
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.sameInstance
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.any
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.verify

/**
 * Unit test for [FcpInterface] and its subclasses.
 */
class FcpInterfaceTest {

	private val core = mock<Core>()
	private val workingCommand = mock<AbstractSoneCommand>().apply {
		whenever(execute(any())).thenReturn(Response("Working", SimpleFieldSet(true).apply {
			putSingle("ReallyWorking", "true")
		}))
	}
	private val brokenCommand = mock<AbstractSoneCommand>().apply {
		whenever(execute(any())).thenThrow(RuntimeException::class.java)
	}
	private val commandSupplier = object : CommandSupplier() {
		override fun supplyCommands(core: Core): Map<String, AbstractSoneCommand> {
			return mapOf(
					"Working" to workingCommand,
					"Broken" to brokenCommand
			)
		}
	}
	private val accessAuthorizer = mock<AccessAuthorizer>()
	private val fcpInterface = FcpInterface(core, commandSupplier, accessAuthorizer)
	private val pluginReplySender = mock<PluginReplySender>()
	private val parameters = SimpleFieldSet(true)
	private val replyParameters = capture<SimpleFieldSet>()

	@Test
	fun `fcp interface is instantiated as singleton`() {
		val injector = Guice.createInjector(Core::class.isProvidedBy(core))
		assertThat(injector.getInstance(FcpInterface::class.java), sameInstance(injector.getInstance(FcpInterface::class.java)))
	}

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
	fun `sending command to inactive fcp interface results in 503 error reply`() {
		fcpInterface.fcpInterfaceDeactivated(FcpInterfaceDeactivatedEvent())
		parameters.putSingle("Identifier", "Test")
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
	fun `sending command over non-authorized connection results in 401 error reply`() {
		fcpInterface.fcpInterfaceActivated(FcpInterfaceActivatedEvent())
		parameters.putSingle("Identifier", "Test")
		parameters.putSingle("Message", "Working")
		fcpInterface.handle(pluginReplySender, parameters, null, RESTRICTED_FCP.ordinal)
		verify(pluginReplySender).send(replyParameters.capture())
		assertThat(replyParameters.value["Message"], equalTo("Error"))
		assertThat(replyParameters.value["ErrorCode"], equalTo("401"))
	}

	@Test
	fun `sending unknown command results in 404 error reply`() {
		fcpInterface.fcpInterfaceActivated(FcpInterfaceActivatedEvent())
		parameters.putSingle("Identifier", "Test")
		fcpInterface.handle(pluginReplySender, parameters, null, RESTRICTED_FCP.ordinal)
		verify(pluginReplySender).send(replyParameters.capture())
		assertThat(replyParameters.value["Message"], equalTo("Error"))
		assertThat(replyParameters.value["ErrorCode"], equalTo("404"))
	}

	@Test
	fun `sending working command without identifier results in 400 error code`() {
		fcpInterface.fcpInterfaceActivated(FcpInterfaceActivatedEvent())
		whenever(accessAuthorizer.authorized(any(), any(), anyBoolean())).thenReturn(true)
		parameters.putSingle("Message", "Working")
		fcpInterface.handle(pluginReplySender, parameters, null, FULL_FCP.ordinal)
		verify(pluginReplySender).send(replyParameters.capture())
		assertThat(replyParameters.value["Message"], equalTo("Error"))
		assertThat(replyParameters.value["ErrorCode"], equalTo("400"))
	}

	@Test
	fun `sending working command with empty identifier results in 400 error code`() {
		fcpInterface.fcpInterfaceActivated(FcpInterfaceActivatedEvent())
		whenever(accessAuthorizer.authorized(any(), any(), anyBoolean())).thenReturn(true)
		parameters.putSingle("Message", "Working")
		parameters.putSingle("Identifier", "")
		fcpInterface.handle(pluginReplySender, parameters, null, FULL_FCP.ordinal)
		verify(pluginReplySender).send(replyParameters.capture())
		assertThat(replyParameters.value["Message"], equalTo("Error"))
		assertThat(replyParameters.value["ErrorCode"], equalTo("400"))
	}

	@Test
	fun `sending working command with identifier results in working reply`() {
		fcpInterface.fcpInterfaceActivated(FcpInterfaceActivatedEvent())
		whenever(accessAuthorizer.authorized(any(), any(), anyBoolean())).thenReturn(true)
		parameters.putSingle("Message", "Working")
		parameters.putSingle("Identifier", "Test")
		fcpInterface.handle(pluginReplySender, parameters, null, FULL_FCP.ordinal)
		verify(pluginReplySender).send(replyParameters.capture())
		assertThat(replyParameters.value["Message"], equalTo("Working"))
		assertThat(replyParameters.value["ReallyWorking"], equalTo("true"))
	}

	@Test
	fun `sending broken  command with identifier results in 500 error reply`() {
		fcpInterface.fcpInterfaceActivated(FcpInterfaceActivatedEvent())
		whenever(accessAuthorizer.authorized(any(), any(), anyBoolean())).thenReturn(true)
		parameters.putSingle("Message", "Broken")
		parameters.putSingle("Identifier", "Test")
		fcpInterface.handle(pluginReplySender, parameters, null, FULL_FCP.ordinal)
		verify(pluginReplySender).send(replyParameters.capture())
		assertThat(replyParameters.value["Message"], equalTo("Error"))
		assertThat(replyParameters.value["ErrorCode"], equalTo("500"))
	}

}

class CommandSupplierTest {

	private val core = mock<Core>()
	private val commandSupplier = CommandSupplier()

	@Test
	fun `command supplier supplies all commands`() {
		val commands = commandSupplier.supplyCommands(core)
		assertThat(commands.keys, containsInAnyOrder(
				"CreatePost",
				"CreateReply",
				"DeletePost",
				"DeleteReply",
				"GetLocalSones",
				"GetPost",
				"GetPostFeed",
				"GetPosts",
				"GetSone",
				"GetSones",
				"LikePost",
				"LikeReply",
				"LockSone",
				"UnlockSone",
				"Version"
		))
	}

	@Test
	fun `command supplier is instantiated as singleton`() {
		val injector = Guice.createInjector()
		assertThat(injector.getInstance(CommandSupplier::class.java), sameInstance(injector.getInstance(CommandSupplier::class.java)))
	}

}

class AccessAuthorizerTest {

	private val accessAuthorizer = AccessAuthorizer()

	@Test
	fun `access authorizer is instantiated as singleton`() {
		val injector = Guice.createInjector()
		assertThat(injector.getInstance(AccessAuthorizer::class.java), sameInstance(injector.getInstance(AccessAuthorizer::class.java)))
	}

	@Test
	fun `access authorizer makes correct decisions`() {
		AccessType.values().forEach { accessType ->
			FullAccessRequired.values().forEach { fullAccessRequired ->
				listOf(false, true).forEach { commandRequiresWriteAccess ->
					assertThat("$accessType, $fullAccessRequired, $commandRequiresWriteAccess", accessAuthorizer.authorized(accessType, fullAccessRequired, commandRequiresWriteAccess), equalTo(
							accessType != RESTRICTED_FCP ||
									fullAccessRequired == NO ||
									(fullAccessRequired == WRITING && !commandRequiresWriteAccess)
					))
				}
			}
		}
	}

}
