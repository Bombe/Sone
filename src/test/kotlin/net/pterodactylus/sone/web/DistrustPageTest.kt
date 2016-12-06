package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [DistrustPage].
 */
class DistrustPageTest : WebPageTest() {

	private val page = DistrustPage(template, webInterface)

	override fun getPage() = page

	@Test
	fun `get request does not redirect`() {
	    request("", GET)
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request with invalid sone redirects to return page`() {
	    request("", POST)
		addHttpRequestParameter("returnPage", "return.html")
		verifyRedirect("return.html")
	}

	@Test
	fun `post request with valid sone distrusts sone and redirects to return page`() {
	    request("", POST)
		val remoteSone = mock<Sone>()
		addSone("remote-sone-id", remoteSone)
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("sone", "remote-sone-id")
		verifyRedirect("return.html") {
			verify(core).distrustSone(currentSone, remoteSone)
		}
	}

}
