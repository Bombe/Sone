package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [LikePage].
 */
class LikePageTest : WebPageTest(::LikePage) {

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("like.html"))
	}

	@Test
	fun `page requires login`() {
		assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `page returns correct title`() {
		addTranslation("Page.Like.Title", "like page title")
		assertThat(page.getPageTitle(soneRequest), equalTo("like page title"))
	}

	@Test
	fun `get request does not redirect`() {
		verifyNoRedirect {}
	}

	@Test
	fun `post request with post id likes post and redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("type", "post")
		addHttpRequestPart("post", "post-id")
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(currentSone).addLikedPostId("post-id")
		}
	}

	@Test
	fun `post request with reply id likes post and redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("type", "reply")
		addHttpRequestPart("reply", "reply-id")
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(currentSone).addLikedReplyId("reply-id")
		}
	}

	@Test
	fun `post request with invalid likes redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("type", "foo")
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html") {
			verifyNoMoreInteractions(currentSone)
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<LikePage>(), notNullValue())
	}

}
