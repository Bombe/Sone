package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Post
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [BookmarkAjaxPage].
 */
class BookmarkAjaxPageTest : JsonPageTest("bookmark.ajax", requiresLogin = false, pageSupplier = ::BookmarkAjaxPage) {

	@Test
	fun `missing post ID results in invalid id response`() {
		assertThat(json.isSuccess, equalTo(false))
		assertThat((json as JsonErrorReturnObject).error, equalTo("invalid-post-id"))
	}

	@Test
	fun `empty post ID results in invalid id response`() {
		addRequestParameter("post", "")
		assertThat(json.isSuccess, equalTo(false))
		assertThat((json as JsonErrorReturnObject).error, equalTo("invalid-post-id"))
	}

	@Test
	fun `invalid post ID results in success but does not bookmark anything`() {
		addRequestParameter("post", "missing")
		assertThat(json.isSuccess, equalTo(true))
		verify(core, never()).bookmarkPost(any<Post>())
	}

	@Test
	fun `valid post ID results in success and bookmarks the post`() {
		addRequestParameter("post", "valid-post-id")
		val post = addNewPost("valid-post-id", "1", 2)
		assertThat(json.isSuccess, equalTo(true))
		verify(core).bookmarkPost(post)
	}

}
