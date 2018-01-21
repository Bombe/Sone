package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.web.baseInjector
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [UnlikePage].
 */
class UnlikePageTest: WebPageTest(::UnlikePage) {

	@Test
	fun `page returns correct path`() {
	    assertThat(page.path, equalTo("unlike.html"))
	}

	@Test
	fun `page requires login`() {
	    assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `page returns correct title`() {
		addTranslation("Page.Unlike.Title", "unlike page title")
	    assertThat(page.getPageTitle(freenetRequest), equalTo("unlike page title"))
	}

	@Test
	fun `get request does not redirect`() {
		verifyNoRedirect { }
	}

	@Test
	fun `post request does not remove any likes but redirects`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(currentSone, never()).removeLikedPostId(any())
			verify(currentSone, never()).removeLikedReplyId(any())
		}
	}

	@Test
	fun `post request removes post like and redirects`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("type", "post")
		addHttpRequestPart("post", "post-id")
		verifyRedirect("return.html") {
			verify(currentSone).removeLikedPostId("post-id")
			verify(currentSone, never()).removeLikedReplyId(any())
		}
	}

	@Test
	fun `post request removes reply like and redirects`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("type", "reply")
		addHttpRequestPart("reply", "reply-id")
		verifyRedirect("return.html") {
			verify(currentSone, never()).removeLikedPostId(any())
			verify(currentSone).removeLikedReplyId("reply-id")
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<UnlikePage>(), notNullValue())
	}

}
