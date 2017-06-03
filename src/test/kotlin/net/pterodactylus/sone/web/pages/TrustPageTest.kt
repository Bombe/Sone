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
 * Unit test for [TrustPage].
 */
class TrustPageTest : WebPageTest() {

	private val page = TrustPage(template, webInterface)

	override fun getPage() = page

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
		assertThat(page.getPageTitle(freenetRequest), equalTo("title trust page"))
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

}
