package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.web.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [UntrustPage].
 */
class UntrustPageTest: WebPageTest(::UntrustPage) {

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
	    assertThat(page.getPageTitle(soneRequest), equalTo("untrust page title"))
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

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<UntrustPage>(), notNullValue())
	}

}
