package net.pterodactylus.sone.web

import net.pterodactylus.sone.web.WebTestUtils.redirectsTo
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import org.junit.Test
import org.mockito.Mockito.verify
import kotlin.test.fail

/**
 * Unit test for [DeleteSonePage].
 */
class DeleteSonePageTest : WebPageTest() {

	private val page = DeleteSonePage(template, webInterface)

	@Test
	fun `get request does not redirect`() {
		request("", GET)
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request without delete confirmation redirects to index`() {
		request("", POST)
		expectedException.expect(redirectsTo("index.html"))
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request with delete confirmation deletes sone and redirects to index`() {
		request("", POST)
		addHttpRequestParameter("deleteSone", "true")
		expectedException.expect(redirectsTo("index.html"))
		try {
			page.handleRequest(freenetRequest, templateContext)
			fail()
		} finally {
			verify(core).deleteSone(currentSone)
		}
	}

}
