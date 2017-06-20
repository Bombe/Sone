package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.utils.Pagination
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test

/**
 * Unit test for [BookmarksPage].
 */
class BookmarksPageTest: WebPageTest2(::BookmarksPage) {

	private val post1 = createLoadedPost(1000)
	private val post2 = createLoadedPost(3000)
	private val post3 = createLoadedPost(2000)

	private fun createLoadedPost(time: Long) = mock<Post>().apply {
		whenever(isLoaded).thenReturn(true)
		whenever(this.time).thenReturn(time)
	}

	@Before
	fun setupBookmarkedPostsAndPagination() {
		whenever(core.bookmarkedPosts).thenReturn(setOf(post1, post2, post3))
		core.preferences.postsPerPage = 5
	}

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("bookmarks.html"))
	}

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `page sets correct posts in template context`() {
		verifyNoRedirect {
			assertThat(templateContext["posts"] as Collection<Post>, contains(post2, post3, post1))
			assertThat((templateContext["pagination"] as Pagination<Post>).items, contains(post2, post3, post1))
			assertThat(templateContext["postsNotLoaded"], equalTo<Any>(false))
		}
	}

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `page does not put unloaded posts in template context but sets a flag`() {
		whenever(post3.isLoaded).thenReturn(false)
		verifyNoRedirect {
			assertThat(templateContext["posts"] as Collection<Post>, contains(post2, post1))
			assertThat((templateContext["pagination"] as Pagination<Post>).items, contains(post2, post1))
			assertThat(templateContext["postsNotLoaded"], equalTo<Any>(true))
		}
	}

}
