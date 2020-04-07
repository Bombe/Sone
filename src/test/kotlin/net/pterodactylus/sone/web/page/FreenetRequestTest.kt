package net.pterodactylus.sone.web.page

import freenet.clients.http.*
import freenet.support.api.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.util.web.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*
import java.net.*

class FreenetRequestTest {

	private val uri = URI(".")
	private val method = Method.GET
	private val httpRequest = mock(HTTPRequest::class.java)
	private val toadletContext = mock(ToadletContext::class.java)
	private val sessionManager = mock<SessionManager>()
	private val request = FreenetRequest(uri, method, httpRequest, toadletContext)

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

}
