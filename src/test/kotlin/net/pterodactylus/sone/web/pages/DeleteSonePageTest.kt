package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.pages.WebPageTest
import net.pterodactylus.sone.web.pages.DeleteSonePage
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [DeleteSonePage].
 */
class DeleteSonePageTest : WebPageTest() {

	private val page = DeleteSonePage(template, webInterface)

	override fun getPage() = page

	@Test
	fun `page returns correct path`() {
	    assertThat(page.path, equalTo("deleteSone.html"))
	}

	@Test
	fun `page requires login`() {
	    assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `page returns correct title`() {
	    whenever(l10n.getString("Page.DeleteSone.Title")).thenReturn("delete sone page")
		assertThat(page.getPageTitle(freenetRequest), equalTo("delete sone page"))
	}

	@Test
	fun `get request does not redirect`() {
		request("", GET)
		page.processTemplate(freenetRequest, templateContext)
	}

	@Test
	fun `post request without delete confirmation redirects to index`() {
		request("", POST)
		verifyRedirect("index.html") {
			verify(core, never()).deleteSone(any())
		}
	}

	@Test
	fun `post request with delete confirmation deletes sone and redirects to index`() {
		request("", POST)
		addHttpRequestPart("deleteSone", "true")
		verifyRedirect("index.html") {
			verify(core).deleteSone(currentSone)
		}
	}

}
