package net.pterodactylus.sone.web

import net.pterodactylus.util.web.Method.POST
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [UnfollowSonePage].
 */
class UnfollowSonePageTest : WebPageTest() {

	private val page = UnfollowSonePage(template, webInterface)

	override fun getPage() = page

	@Test
	fun `get request does not redirect`() {
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request unfollows a single sone and redirects to return page`() {
		request("", POST)
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("sone", "sone-id")
		verifyRedirect("return.html") {
			verify(core).unfollowSone(currentSone, "sone-id")
		}
	}

	@Test
	fun `post request unfollows two sones and redirects to return page`() {
		request("", POST)
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("sone", "sone-id1, sone-id2")
		verifyRedirect("return.html") {
			verify(core).unfollowSone(currentSone, "sone-id1")
			verify(core).unfollowSone(currentSone, "sone-id2")
		}
	}

}
