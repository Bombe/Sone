package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.web.baseInjector
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
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
		assertThatJsonFailed("invalid-post-id")
	}

	@Test
	fun `empty post ID results in invalid id response`() {
		addRequestParameter("post", "")
		assertThatJsonFailed("invalid-post-id")
	}

	@Test
	fun `invalid post ID results in success but does not bookmark anything`() {
		addRequestParameter("post", "missing")
		assertThatJsonIsSuccessful()
		verify(core, never()).bookmarkPost(any<Post>())
	}

	@Test
	fun `valid post ID results in success and bookmarks the post`() {
		addRequestParameter("post", "valid-post-id")
		val post = addNewPost("valid-post-id", "1", 2)
		assertThatJsonIsSuccessful()
		verify(core).bookmarkPost(post)
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<BookmarkAjaxPage>(), notNullValue())
	}

}
