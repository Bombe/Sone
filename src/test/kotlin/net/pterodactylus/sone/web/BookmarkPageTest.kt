package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.test.mock
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [BookmarkPage].
 */
class BookmarkPageTest : WebPageTest() {

	private val page = BookmarkPage(template, webInterface)
	override fun getPage() = page

	@Test
	fun `path is set correctly`() {
		assertThat(page.path, equalTo("bookmark.html"))
	}

	@Test
	fun `get request does not bookmark anything and does not redirect`() {
		page.processTemplate(freenetRequest, templateContext)
		verify(core, never()).bookmarkPost(any())
	}

	private fun setupBookmarkRequest() {
		request("", POST)
		addHttpRequestParameter("returnPage", "return-page.html")
		addHttpRequestParameter("post", "post-id")
	}

	@Test
	fun `post is bookmarked correctly`() {
		setupBookmarkRequest()
		val post = mock<Post>()
		addPost("post-id", post)
		verifyRedirect("return-page.html") {
			verify(core).bookmarkPost(post)
		}
	}

	@Test
	fun `non-existing post is not bookmarked`() {
		setupBookmarkRequest()
		verifyRedirect("return-page.html") {
			verify(core, never()).bookmarkPost(any())
		}
	}

}
