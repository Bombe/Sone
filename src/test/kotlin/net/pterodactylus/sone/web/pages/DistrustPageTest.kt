package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [DistrustPage].
 */
class DistrustPageTest : WebPageTest(::DistrustPage) {

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("distrust.html"))
	}

	@Test
	fun `page requires login`() {
		assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `page returns correct title`() {
		whenever(l10n.getString("Page.Distrust.Title")).thenReturn("distrust page title")
		assertThat(page.getPageTitle(soneRequest), equalTo("distrust page title"))
	}

	@Test
	fun `get request does not redirect`() {
		page.processTemplate(freenetRequest, templateContext)
	}

	@Test
	fun `post request with invalid sone redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html")
	}

	@Test
	fun `post request with valid sone distrusts sone and redirects to return page`() {
		setMethod(POST)
		val remoteSone = mock<Sone>()
		addSone("remote-sone-id", remoteSone)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("sone", "remote-sone-id")
		verifyRedirect("return.html") {
			verify(core).distrustSone(currentSone, remoteSone)
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<DistrustPage>(), notNullValue())
	}

}
