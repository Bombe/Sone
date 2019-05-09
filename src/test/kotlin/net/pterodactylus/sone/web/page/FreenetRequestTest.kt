package net.pterodactylus.sone.web.page

import freenet.clients.http.*
import freenet.clients.http.SessionManager.*
import freenet.l10n.*
import freenet.support.api.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.util.web.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.*
import org.mockito.Mockito.*
import java.net.*

class FreenetRequestTest {

	private val uri = URI(".")
	private val method = Method.GET
	private val httpRequest = mock(HTTPRequest::class.java)
	private val toadletContext = mock(ToadletContext::class.java)
	private val l10n = mock<BaseL10n>()
	private val sessionManager = mock<SessionManager>()
	private val request = FreenetRequest(uri, method, httpRequest, toadletContext, l10n, sessionManager)

	@Test
	fun `uri is retained correctly`() {
		assertThat(request.uri, equalTo(uri))
	}

	@Test
	fun `method is retained correctly`() {
		assertThat(request.method, equalTo(method))
	}

	@Test
	fun `http request is retained correctly`() {
		assertThat(request.httpRequest, equalTo(httpRequest))
	}

	@Test
	fun `toadlet context is retained correctly`() {
		assertThat(request.toadletContext, equalTo(toadletContext))
	}

	@Test
	fun `l10n is retained correctly`() {
		assertThat(request.l10n, equalTo(l10n))
	}

	@Test
	fun `null is returned if no session exists`() {
		assertThat(request.existingSession, nullValue())
	}

	@Test
	fun `existing session can be retrieved`() {
		val session = mock<Session>()
		whenever(sessionManager.useSession(toadletContext)).thenReturn(session)
		assertThat(request.existingSession, sameInstance(session))
	}

	@Test
	fun `existing session is returned if it exists`() {
		val session = mock<Session>()
		whenever(sessionManager.useSession(toadletContext)).thenReturn(session)
		assertThat(request.session, sameInstance(session))
	}

	@Test
	fun `new session is returned if none exists`() {
		val session = mock<Session>()
		whenever(sessionManager.createSession(anyString(), ArgumentMatchers.eq(toadletContext))).thenReturn(session)
		assertThat(request.session, sameInstance(session))
	}

}
