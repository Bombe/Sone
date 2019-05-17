package net.pterodactylus.sone.web.pages

import com.google.common.base.*
import com.google.common.base.Optional.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.notify.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.*

/**
 * Unit test for [IndexPage].
 */
class IndexPageTest : WebPageTest({ webInterface, loaders, templateRenderer -> IndexPage(webInterface, loaders, templateRenderer, postVisibilityFilter) }) {

	companion object {
		private val postVisibilityFilter = mock<PostVisibilityFilter>()
	}

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("index.html"))
	}

	@Test
	fun `page requires login`() {
		assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `page returns correct title`() {
		whenever(l10n.getString("Page.Index.Title")).thenReturn("index page title")
		assertThat(page.getPageTitle(soneRequest), equalTo("index page title"))
	}

	@Before
	fun setupPostVisibilityFilter() {
		whenever(postVisibilityFilter.isVisible(ArgumentMatchers.eq(currentSone))).thenReturn(Predicate<Post> { true })
	}

	@Before
	fun setupCurrentSone() {
		whenever(currentSone.id).thenReturn("current")
	}

	@Before
	fun setupDirectedPosts() {
		whenever(core.getDirectedPosts("current")).thenReturn(emptyList())
	}

	private fun createPost(time: Long, directed: Boolean = false) = mock<Post>().apply {
		whenever(this.time).thenReturn(time)
		whenever(recipient).thenReturn(fromNullable(if (directed) currentSone else null))
	}

	@Test
	fun `index page shows all posts of current sone`() {
		val posts = listOf(createPost(3000), createPost(2000), createPost(1000))
		whenever(currentSone.posts).thenReturn(posts)
		page.processTemplate(freenetRequest, templateContext)
		@Suppress("UNCHECKED_CAST")
		assertThat(templateContext["posts"] as Iterable<Post>, contains(*posts.toTypedArray()))
	}

	@Test
	fun `index page shows posts directed at current sone from non-followed sones`() {
		val posts = listOf(createPost(3000), createPost(2000), createPost(1000))
		whenever(currentSone.posts).thenReturn(posts)
		val notFollowedSone = mock<Sone>()
		val notFollowedPosts = listOf(createPost(2500, true), createPost(1500))
		whenever(notFollowedSone.posts).thenReturn(notFollowedPosts)
		addSone("notfollowed1", notFollowedSone)
		whenever(core.getDirectedPosts("current")).thenReturn(listOf(notFollowedPosts[0]))
		page.processTemplate(freenetRequest, templateContext)
		@Suppress("UNCHECKED_CAST")
		assertThat(templateContext["posts"] as Iterable<Post>, contains(
				posts[0], notFollowedPosts[0], posts[1], posts[2]
		))
	}

	@Test
	fun `index page does not show duplicate posts`() {
		val posts = listOf(createPost(3000), createPost(2000), createPost(1000))
		whenever(currentSone.posts).thenReturn(posts)
		val followedSone = mock<Sone>()
		val followedPosts = listOf(createPost(2500, true), createPost(1500))
		whenever(followedSone.posts).thenReturn(followedPosts)
		whenever(currentSone.friends).thenReturn(listOf("followed1", "followed2"))
		addSone("followed1", followedSone)
		page.processTemplate(freenetRequest, templateContext)
		@Suppress("UNCHECKED_CAST")
		assertThat(templateContext["posts"] as Iterable<Post>, contains(
				posts[0], followedPosts[0], posts[1], followedPosts[1], posts[2]
		))
	}

	@Test
	fun `index page uses post visibility filter`() {
		val posts = listOf(createPost(3000), createPost(2000), createPost(1000))
		whenever(currentSone.posts).thenReturn(posts)
		val followedSone = mock<Sone>()
		val followedPosts = listOf(createPost(2500, true), createPost(1500))
		whenever(followedSone.posts).thenReturn(followedPosts)
		whenever(currentSone.friends).thenReturn(listOf("followed1", "followed2"))
		whenever(postVisibilityFilter.isVisible(ArgumentMatchers.eq(currentSone))).thenReturn(Predicate<Post> { (it?.time ?: 10000) < 2500 })
		addSone("followed1", followedSone)
		page.processTemplate(freenetRequest, templateContext)
		@Suppress("UNCHECKED_CAST")
		assertThat(templateContext["posts"] as Iterable<Post>, contains(
				posts[1], followedPosts[1], posts[2]
		))
	}

	@Test
	fun `index page sets pagination correctly`() {
		val posts = listOf(createPost(3000), createPost(2000), createPost(1000))
		whenever(currentSone.posts).thenReturn(posts)
		page.processTemplate(freenetRequest, templateContext)
		@Suppress("UNCHECKED_CAST")
		assertThat((templateContext["pagination"] as Pagination<Post>).items, contains(
				posts[0], posts[1], posts[2]
		))
	}

	@Test
	fun `index page sets page correctly`() {
		val posts = listOf(createPost(3000), createPost(2000), createPost(1000))
		whenever(currentSone.posts).thenReturn(posts)
		core.preferences.newPostsPerPage = 1
		addHttpRequestParameter("page", "2")
		page.processTemplate(freenetRequest, templateContext)
		@Suppress("UNCHECKED_CAST")
		assertThat((templateContext["pagination"] as Pagination<Post>).page, equalTo(2))
	}

	@Test
	fun `index page without posts sets correct pagination`() {
		core.preferences.newPostsPerPage = 1
		page.processTemplate(freenetRequest, templateContext)
		@Suppress("UNCHECKED_CAST")
		(templateContext["pagination"] as Pagination<Post>).let { pagination ->
			assertThat(pagination.items, emptyIterable())
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<IndexPage>(), notNullValue())
	}

	@Test
	fun `page is annotated with correct menuname`() {
		assertThat(page.menuName, equalTo("Index"))
	}

	@Test
	fun `page is annotated with correct template path`() {
		assertThat(page.templatePath, equalTo("/templates/index.html"))
	}

}
