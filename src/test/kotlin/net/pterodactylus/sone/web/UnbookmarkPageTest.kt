package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method.POST
import org.junit.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [UnbookmarkPage].
 */
class UnbookmarkPageTest : WebPageTest() {

	private val page = UnbookmarkPage(template, webInterface)

	override fun getPage() = page

	@Test
	fun `get request does not redirect`() {
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `get request with all-not-loaded parameter unloads all not loaded posts and redirects to bookmarks`() {
		addHttpRequestParameter("post", "allNotLoaded")
		val loadedPost1 = mock<Post>().apply { whenever(isLoaded).thenReturn(true) }
		val loadedPost2 = mock<Post>().apply { whenever(isLoaded).thenReturn(true) }
		val notLoadedPost1 = mock<Post>()
		val notLoadedPost2 = mock<Post>()
		whenever(core.bookmarkedPosts).thenReturn(setOf(loadedPost1, loadedPost2, notLoadedPost1, notLoadedPost2))
		verifyRedirect("bookmarks.html") {
			verify(core).unbookmarkPost(notLoadedPost1)
			verify(core).unbookmarkPost(notLoadedPost2)
			verify(core, never()).unbookmarkPost(loadedPost1)
			verify(core, never()).unbookmarkPost(loadedPost2)
		}
	}

	@Test
	fun `post request does not unbookmark not-present post but redirects to return page`() {
		request("", POST)
		addHttpRequestParameter("post", "post-id")
		addHttpRequestParameter("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(core, never()).unbookmarkPost(any())
		}
	}

	@Test
	fun `post request unbookmarks present post and redirects to return page`() {
		request("", POST)
		addHttpRequestParameter("post", "post-id")
		addHttpRequestParameter("returnPage", "return.html")
		val post = mock<Post>().apply { whenever(isLoaded).thenReturn(true) }
		addPost("post-id", post)
		verifyRedirect("return.html") {
			verify(core).unbookmarkPost(post)
		}
	}

}
