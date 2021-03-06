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
 * Unit test for [BookmarkPage].
 */
class BookmarkPageTest : WebPageTest(::BookmarkPage) {

	@Test
	fun `path is set correctly`() {
		assertThat(page.path, equalTo("bookmark.html"))
	}

	@Test
	fun `get request does not bookmark anything and does not redirect`() {
		verifyNoRedirect {
			verify(core, never()).bookmarkPost(any())
		}
	}

	private fun setupBookmarkRequest() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return-page.html")
		addHttpRequestPart("post", "post-id")
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

	@Test
	fun `bookmark page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<BookmarkPage>(), notNullValue())
	}

}
