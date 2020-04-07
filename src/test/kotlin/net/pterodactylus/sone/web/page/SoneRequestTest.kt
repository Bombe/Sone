package net.pterodactylus.sone.web.page

import freenet.clients.http.*
import freenet.l10n.*
import freenet.support.api.*
import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.util.web.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.*
import java.net.*

class SoneRequestTest {

	private val uri = URI(".")
	private val method = Method.GET
	private val httpRequest = Mockito.mock(HTTPRequest::class.java)
	private val toadletContext = Mockito.mock(ToadletContext::class.java)
	private val core = mock<Core>()
	private val webInterface = mock<WebInterface>()
	private val soneRequest = SoneRequest(uri, method, httpRequest, toadletContext, core, webInterface)

	@Test
	fun `freenet request properties are retained correctly`() {
		assertThat(soneRequest.uri, equalTo(uri))
		assertThat(soneRequest.method, equalTo(method))
		assertThat(soneRequest.httpRequest, equalTo(httpRequest))
		assertThat(soneRequest.toadletContext, equalTo(toadletContext))
	}

	@Test
	fun `core is retained correctly`() {
		assertThat(soneRequest.core, sameInstance(core))
	}

	@Test
	fun `web interface is retained correctly`() {
		assertThat(soneRequest.webInterface, sameInstance(webInterface))
	}

	@Test
	fun `freenet request is wrapped correctly`() {
	    val freenetRequest = FreenetRequest(uri, method, httpRequest, toadletContext)
		val wrappedSoneRequest = freenetRequest.toSoneRequest(core, webInterface)
		assertThat(wrappedSoneRequest.uri, equalTo(uri))
		assertThat(wrappedSoneRequest.method, equalTo(method))
		assertThat(wrappedSoneRequest.httpRequest, equalTo(httpRequest))
		assertThat(wrappedSoneRequest.toadletContext, equalTo(toadletContext))
		assertThat(wrappedSoneRequest.core, sameInstance(core))
		assertThat(wrappedSoneRequest.webInterface, sameInstance(webInterface))
	}

}
