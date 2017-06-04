package net.pterodactylus.sone.web.pages

import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [UnfollowSonePage].
 */
class UnfollowSonePageTest : WebPageTest() {

	private val page = UnfollowSonePage(template, webInterface)

	override fun getPage() = page

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
		assertThat(page.getPageTitle(freenetRequest), equalTo("unfollow page title"))
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

}
