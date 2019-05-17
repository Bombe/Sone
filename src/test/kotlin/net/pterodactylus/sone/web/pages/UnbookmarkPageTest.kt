package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [UnbookmarkPage].
 */
class UnbookmarkPageTest: WebPageTest(::UnbookmarkPage) {

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("unbookmark.html"))
	}

	@Test
	fun `page does not require login`() {
		assertThat(page.requiresLogin(), equalTo(false))
	}

	@Test
	fun `page returns correct title`() {
		addTranslation("Page.Unbookmark.Title", "unbookmark page title")
		assertThat(page.getPageTitle(soneRequest), equalTo("unbookmark page title"))
	}

	@Test
	fun `get request does not redirect`() {
		verifyNoRedirect { }
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
			val postCaptor = capture<Post>()
			verify(core, times(2)).unbookmarkPost(postCaptor.capture())
			assertThat(postCaptor.allValues, contains(notLoadedPost1, notLoadedPost2))
		}
	}

	@Test
	fun `post request does not unbookmark not-present post but redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("post", "post-id")
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("return.html") {
			verify(core, never()).unbookmarkPost(any())
		}
	}

	@Test
	fun `post request unbookmarks present post and redirects to return page`() {
		setMethod(POST)
		addHttpRequestPart("post", "post-id")
		addHttpRequestPart("returnPage", "return.html")
		val post = mock<Post>().apply { whenever(isLoaded).thenReturn(true) }
		addPost("post-id", post)
		verifyRedirect("return.html") {
			verify(core).unbookmarkPost(post)
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<UnbookmarkPage>(), notNullValue())
	}

}
