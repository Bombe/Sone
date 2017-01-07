package net.pterodactylus.sone.fcp

import com.google.common.base.Optional
import com.google.common.base.Optional.absent
import freenet.support.SimpleFieldSet
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.freenet.fcp.Command
import net.pterodactylus.sone.freenet.fcp.FcpException
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString

/**
 * TODO
 */
abstract class SoneCommandTest {

	@Rule @JvmField val expectedException = ExpectedException.none()!!

	protected val core = mock<Core>()
	protected val command: AbstractSoneCommand by lazy { createCommand(core) }

	protected val parameters = SimpleFieldSet(true)
	protected val localSone = mock<Sone>().apply {
		whenever(isLocal).thenReturn(true)
	}
	protected val remoteSone = mock<Sone>()

	protected abstract fun createCommand(core: Core): AbstractSoneCommand

	@Before
	fun setupCore() {
		whenever(core.getSone(anyString())).thenReturn(absent())
	}

	protected fun requestWithoutAnyParameterResultsInFcpException() {
		expectedException.expect(FcpException::class.java)
		command.execute(parameters, null, null)
	}

	protected fun requestWithEmptySoneParameterResultsInFcpException() {
		parameters.putSingle("Sone", null)
		expectedException.expect(FcpException::class.java)
		command.execute(parameters, null, null)
	}

	protected fun requestWithInvalidSoneParameterResultsInFcpException() {
		parameters.putSingle("Sone", "InvalidSoneId")
		expectedException.expect(FcpException::class.java)
		command.execute(parameters, null, null)
	}

	fun requestWithValidRemoteSoneParameterResultsInFcpException() {
		parameters.putSingle("Sone", "RemoteSoneId")
		whenever(core.getSone("RemoteSoneId")).thenReturn(Optional.of(remoteSone))
		expectedException.expect(FcpException::class.java)
		command.execute(parameters, null, null)
	}

}
