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
 * Unit test for [TrustPage].
 */
class TrustPageTest: WebPageTest(::TrustPage) {

	@Test
	fun `page returns correct path`() {
	    assertThat(page.path, equalTo("trust.html"))
	}

	@Test
	fun `page requires login`() {
	    assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `page returns correct title`() {
	    addTranslation("Page.Trust.Title", "title trust page")
		assertThat(page.getPageTitle(soneRequest), equalTo("title trust page"))
	}

	@Test
	fun `get method does not redirect`() {
		verifyNoRedirect { }
	}

	@Test
	fun `post request without sone redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(core, never()).trustSone(eq(currentSone), any())
		}
	}

	@Test
	fun `post request with missing sone redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("sone", "sone-id")
		verifyRedirect("return.html") {
			verify(core, never()).trustSone(eq(currentSone), any())
		}
	}

	@Test
	fun `post request with existing sone trusts the identity and redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("sone", "sone-id")
		val sone = mock<Sone>()
		addSone("sone-id", sone)
		verifyRedirect("return.html") {
			verify(core).trustSone(eq(currentSone), eq(sone))
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<TrustPage>(), notNullValue())
	}

}
