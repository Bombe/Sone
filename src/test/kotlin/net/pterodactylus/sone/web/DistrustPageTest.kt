package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.web.WebTestUtils.redirectsTo
import net.pterodactylus.util.web.Method
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify

/**
 * Unit test for [DistrustPage].
 */
class DistrustPageTest : WebPageTest() {

	private val page = DistrustPage(template, webInterface)

	@Test
	fun `get request does not redirect`() {
	    request("", GET)
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request with invalid sone redirects to return page`() {
	    request("", POST)
		addHttpRequestParameter("returnPage", "return.html")
		expectedException.expect(redirectsTo("return.html"))
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request with valid sone distrusts sone and redirects to return page`() {
	    request("", POST)
		val remoteSone = mock<Sone>()
		addSone("remote-sone-id", remoteSone)
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("sone", "remote-sone-id")
		expectedException.expect(redirectsTo("return.html"))
		try {
			page.handleRequest(freenetRequest, templateContext)
		} finally {
			verify(core).distrustSone(currentSone, remoteSone)
		}
	}

}
