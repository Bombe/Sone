package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.web.pages.UntrustPage
import net.pterodactylus.util.web.Method.POST
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [UntrustPage].
 */
class UntrustPageTest : WebPageTest() {

	private val page = UntrustPage(template, webInterface)

	override fun getPage() = page

	@Test
	fun `get request does not redirect`() {
		page.handleRequest(freenetRequest, templateContext)
		verify(core, never()).untrustSone(eq(currentSone), any())
	}

	@Test
	fun `post request without sone parameter does not untrust but redirects`() {
		request("", POST)
		addHttpRequestParameter("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(core, never()).untrustSone(eq(currentSone), any())
		}
	}

	@Test
	fun `post request with invalid sone parameter does not untrust but redirects`() {
		request("", POST)
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("sone", "no-sone")
		verifyRedirect("return.html") {
			verify(core, never()).untrustSone(eq(currentSone), any())
		}
	}

	@Test
	fun `post request with valid sone parameter untrusts and redirects`() {
		request("", POST)
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("sone", "sone-id")
		val sone = mock<Sone>()
		addSone("sone-id", sone)
		verifyRedirect("return.html") {
			verify(core).untrustSone(currentSone, sone)
		}
	}

}
