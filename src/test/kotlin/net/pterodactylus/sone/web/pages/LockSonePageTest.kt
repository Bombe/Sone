package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [LockSonePage].
 */
class LockSonePageTest : WebPageTest(::LockSonePage) {

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("lockSone.html"))
	}

	@Test
	fun `page does not require login`() {
		assertThat(page.requiresLogin(), equalTo(false))
	}

	@Test
	fun `page returns correct title`() {
		addTranslation("Page.LockSone.Title", "lock Sone page title")
		assertThat(page.getPageTitle(soneRequest), equalTo("lock Sone page title"))
	}

	@Test
	fun `locking an invalid local sone redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(core, never()).lockSone(any<Sone>())
		}
	}

	@Test
	fun `locking an valid local sone locks the sone and redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("sone", "sone-id")
		val sone = mock<Sone>()
		addLocalSone("sone-id", sone)
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(core).lockSone(sone)
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<LockSonePage>(), notNullValue())
	}

}
