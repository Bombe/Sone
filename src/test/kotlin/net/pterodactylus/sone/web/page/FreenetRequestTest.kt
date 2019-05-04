package net.pterodactylus.sone.web.page

import freenet.clients.http.*
import freenet.l10n.*
import freenet.support.api.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.util.web.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*
import org.mockito.Mockito.mock
import java.net.*

class FreenetRequestTest {

	private val uri = URI(".")
	private val method = Method.GET
	private val httpRequest = mock(HTTPRequest::class.java)
	private val toadletContext = mock(ToadletContext::class.java)
	private val l10n = mock<BaseL10n>()
	private val request = FreenetRequest(uri, method, httpRequest, toadletContext, l10n)

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

}
