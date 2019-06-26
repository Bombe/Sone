package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [LogoutPage].
 */
class LogoutPageTest : WebPageTest(::LogoutPage) {

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("logout.html"))
	}

	@Test
	fun `page requires login`() {
		assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `page returns correct title`() {
		addTranslation("Page.Logout.Title", "logout page title")
		assertThat(page.getPageTitle(soneRequest), equalTo("logout page title"))
	}

	@Test
	fun `page unsets current sone and redirects to index`() {
		verifyRedirect("index.html") {
			verify(webInterface).setCurrentSone(toadletContext, null)
		}
	}

	@Test
	fun `page is not enabled if sone requires full access and request does not have full access`() {
		core.preferences.newRequireFullAccess = true
		assertThat(page.isEnabled(toadletContext), equalTo(false))
	}

	@Test
	fun `page is disabled if no sone is logged in`() {
		unsetCurrentSone()
		assertThat(page.isEnabled(toadletContext), equalTo(false))
	}

	@Test
	fun `page is disabled if sone is logged in but there is only one sone`() {
		whenever(core.localSones).thenReturn(listOf(currentSone))
		assertThat(page.isEnabled(toadletContext), equalTo(false))
	}

	@Test
	fun `page is enabled if sone is logged in and there is more than one sone`() {
		whenever(core.localSones).thenReturn(listOf(currentSone, currentSone))
		assertThat(page.isEnabled(toadletContext), equalTo(true))
	}

	@Test
	fun `page is enabled if full access is required and present and sone is logged in and there is more than one sone`() {
		core.preferences.newRequireFullAccess = true
		whenever(toadletContext.isAllowedFullAccess).thenReturn(true)
		whenever(core.localSones).thenReturn(listOf(currentSone, currentSone))
		assertThat(page.isEnabled(toadletContext), equalTo(true))
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<LogoutPage>(), notNullValue())
	}

	@Test
	fun `page is annotated with correct menuname`() {
		assertThat(page.menuName, equalTo("Logout"))
	}

}
