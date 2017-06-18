package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [UnlockSonePage].
 */
class UnlockSonePageTest: WebPageTest2(::UnlockSonePage) {

	@Test
	fun `page returns correct path`() {
	    assertThat(page.path, equalTo("unlockSone.html"))
	}

	@Test
	fun `page does not require login`() {
	    assertThat(page.requiresLogin(), equalTo(false))
	}

	@Test
	fun `page returns correct title`() {
		addTranslation("Page.UnlockSone.Title", "unlock page title")
	    assertThat(page.getPageTitle(freenetRequest), equalTo("unlock page title"))
	}

	@Test
	fun `post request without sone redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(core, never()).unlockSone(any())
		}
	}

	@Test
	fun `post request without invalid local sone does not unlock any sone and redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("sone", "invalid-sone")
		verifyRedirect("return.html") {
			verify(core, never()).unlockSone(any())
		}
	}

	@Test
	fun `post request without remote sone does not unlock any sone and redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("sone", "remote-sone")
		addSone("remote-sone", mock<Sone>())
		verifyRedirect("return.html") {
			verify(core, never()).unlockSone(any())
		}
	}

	@Test
	fun `post request with local sone unlocks sone and redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("sone", "local-sone")
		val sone = mock<Sone>().apply { whenever(isLocal).thenReturn(true) }
		addLocalSone("local-sone", sone)
		verifyRedirect("return.html") {
			verify(core).unlockSone(sone)
		}
	}

}
