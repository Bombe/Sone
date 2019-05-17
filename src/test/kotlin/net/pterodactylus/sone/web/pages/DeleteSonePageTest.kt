package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [DeleteSonePage].
 */
class DeleteSonePageTest : WebPageTest(::DeleteSonePage) {

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

	@Test
	fun `page is annotated with correct menuname`() {
		assertThat(page.menuName, equalTo("DeleteSone"))
	}

	@Test
	fun `page is annotated with correct template path`() {
		assertThat(page.templatePath, equalTo("/templates/deleteSone.html"))
	}

}
