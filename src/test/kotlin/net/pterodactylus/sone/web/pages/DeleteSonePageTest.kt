package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.baseInjector
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [DeleteSonePage].
 */
class DeleteSonePageTest: WebPageTest(::DeleteSonePage) {

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
		assertThat(page.getPageTitle(soneRequest), equalTo("delete sone page"))
	}

	@Test
	fun `get request does not redirect`() {
		page.processTemplate(freenetRequest, templateContext)
	}

	@Test
	fun `post request without delete confirmation redirects to index`() {
		setMethod(POST)
		verifyRedirect("index.html") {
			verify(core, never()).deleteSone(any())
		}
	}

	@Test
	fun `post request with delete confirmation deletes sone and redirects to index`() {
		setMethod(POST)
		addHttpRequestPart("deleteSone", "true")
		verifyRedirect("index.html") {
			verify(core).deleteSone(currentSone)
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<DeleteSonePage>(), notNullValue())
	}

}
