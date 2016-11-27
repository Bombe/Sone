package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [LockSonePage].
 */
class LockSonePageTest : WebPageTest() {

	private val page = LockSonePage(template, webInterface)

	@Test
	fun `locking an invalid local sone redirects to return page`() {
		addHttpRequestParameter("returnPage", "return.html")
		expectedException.expect(WebTestUtils.redirectsTo("return.html"))
		try {
			page.handleRequest(freenetRequest, templateContext)
		} finally {
			verify(core, never()).lockSone(any<Sone>())
		}
	}

	@Test
	fun `locking an valid local sone locks the sone and redirects to return page`() {
		addHttpRequestParameter("sone", "sone-id")
		val sone = mock<Sone>()
		addLocalSone("sone-id", sone)
		addHttpRequestParameter("returnPage", "return.html")
		expectedException.expect(WebTestUtils.redirectsTo("return.html"))
		try {
			page.handleRequest(freenetRequest, templateContext)
		} finally {
			verify(core).lockSone(sone)
		}
	}

}
