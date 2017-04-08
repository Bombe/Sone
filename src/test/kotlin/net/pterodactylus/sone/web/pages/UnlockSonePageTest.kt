package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.pages.UnlockSonePage
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [UnlockSonePage].
 */
class UnlockSonePageTest : WebPageTest() {

	private val page = UnlockSonePage(template, webInterface)

	override fun getPage() = page

	@Test
	fun `get request without sone redirects to return page`() {
		addHttpRequestParameter("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(core, never()).unlockSone(any())
		}
	}

	@Test
	fun `get request without invalid local sone does not unlock any sone and redirects to return page`() {
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("sone", "invalid-sone")
		verifyRedirect("return.html") {
			verify(core, never()).unlockSone(any())
		}
	}

	@Test
	fun `get request without remote sone does not unlock any sone and redirects to return page`() {
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("sone", "remote-sone")
		addSone("remote-sone", mock<Sone>())
		verifyRedirect("return.html") {
			verify(core, never()).unlockSone(any())
		}
	}

	@Test
	fun `get request with local sone unlocks sone and redirects to return page`() {
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("sone", "local-sone")
		val sone = mock<Sone>().apply { whenever(isLocal).thenReturn(true) }
		addLocalSone("local-sone", sone)
		verifyRedirect("return.html") {
			verify(core).unlockSone(sone)
		}
	}

}
