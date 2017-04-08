package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.web.pages.LikePage
import net.pterodactylus.sone.web.pages.WebPageTest
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions

/**
 * Unit test for [LikePage].
 */
class LikePageTest : WebPageTest() {

	private val page = LikePage(template, webInterface)

	override fun getPage() = page

	@Test
	fun `get request does not redirect`() {
		request("", GET)
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request with post id likes post and redirects to return page`() {
		request("", POST)
		addHttpRequestParameter("type", "post")
		addHttpRequestParameter("post", "post-id")
		addHttpRequestParameter("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(currentSone).addLikedPostId("post-id")
		}
	}

	@Test
	fun `post request with reply id likes post and redirects to return page`() {
		request("", POST)
		addHttpRequestParameter("type", "reply")
		addHttpRequestParameter("reply", "reply-id")
		addHttpRequestParameter("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(currentSone).addLikedReplyId("reply-id")
		}
	}

	@Test
	fun `post request with invalid likes redirects to return page`() {
		request("", POST)
		addHttpRequestParameter("type", "foo")
		addHttpRequestParameter("returnPage", "return.html")
		verifyRedirect("return.html") {
			verifyNoMoreInteractions(currentSone)
		}
	}

}
