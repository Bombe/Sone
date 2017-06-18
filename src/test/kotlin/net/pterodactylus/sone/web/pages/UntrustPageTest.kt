package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [UntrustPage].
 */
class UntrustPageTest: WebPageTest2(::UntrustPage) {

	@Test
	fun `page returns correct path`() {
	    assertThat(page.path, equalTo("untrust.html"))
	}

	@Test
	fun `page requires login`() {
	    assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `page returns correct title`() {
		addTranslation("Page.Untrust.Title", "untrust page title")
	    assertThat(page.getPageTitle(freenetRequest), equalTo("untrust page title"))
	}

	@Test
	fun `get request does not redirect`() {
		verifyNoRedirect {
			verify(core, never()).untrustSone(eq(currentSone), any())
		}
	}

	@Test
	fun `post request without sone parameter does not untrust but redirects`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(core, never()).untrustSone(eq(currentSone), any())
		}
	}

	@Test
	fun `post request with invalid sone parameter does not untrust but redirects`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("sone", "no-sone")
		verifyRedirect("return.html") {
			verify(core, never()).untrustSone(eq(currentSone), any())
		}
	}

	@Test
	fun `post request with valid sone parameter untrusts and redirects`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("sone", "sone-id")
		val sone = mock<Sone>()
		addSone("sone-id", sone)
		verifyRedirect("return.html") {
			verify(core).untrustSone(currentSone, sone)
		}
	}

}
