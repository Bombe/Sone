package net.pterodactylus.sone.web

import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [DeleteSonePage].
 */
class DeleteSonePageTest : WebPageTest() {

	private val page = DeleteSonePage(template, webInterface)

	override fun getPage() = page

	@Test
	fun `get request does not redirect`() {
		request("", GET)
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request without delete confirmation redirects to index`() {
		request("", POST)
		verifyRedirect("index.html")
	}

	@Test
	fun `post request with delete confirmation deletes sone and redirects to index`() {
		request("", POST)
		addHttpRequestParameter("deleteSone", "true")
		verifyRedirect("index.html") {
			verify(core).deleteSone(currentSone)
		}
	}

}
