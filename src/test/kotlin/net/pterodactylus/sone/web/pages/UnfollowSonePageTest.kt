package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [UnfollowSonePage].
 */
class UnfollowSonePageTest : WebPageTest(::UnfollowSonePage) {

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("unfollowSone.html"))
	}

	@Test
	fun `page requires login`() {
		assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `page returns correct page title`() {
		addTranslation("Page.UnfollowSone.Title", "unfollow page title")
		assertThat(page.getPageTitle(soneRequest), equalTo("unfollow page title"))
	}

	@Test
	fun `get request does not redirect`() {
		verifyNoRedirect { }
	}

	@Test
	fun `post request unfollows a single sone and redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("sone", "sone-id")
		verifyRedirect("return.html") {
			verify(core).unfollowSone(currentSone, "sone-id")
		}
	}

	@Test
	fun `post request unfollows two sones and redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("sone", "sone-id1, sone-id2")
		verifyRedirect("return.html") {
			verify(core).unfollowSone(currentSone, "sone-id1")
			verify(core).unfollowSone(currentSone, "sone-id2")
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<UnfollowSonePage>(), notNullValue())
	}

}
