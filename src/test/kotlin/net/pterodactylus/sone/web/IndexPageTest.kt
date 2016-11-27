package net.pterodactylus.sone.web

import com.google.common.base.Optional.fromNullable
import com.google.common.base.Predicate
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.notify.PostVisibilityFilter
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method.GET
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers

/**
 * Unit test for [IndexPage].
 */
class IndexPageTest : WebPageTest() {

	private val postVisibilityFilter = mock<PostVisibilityFilter>()
	private val page = IndexPage(template, webInterface, postVisibilityFilter)

	@Before
	fun setupPostVisibilityFilter() {
		whenever(postVisibilityFilter.isVisible(ArgumentMatchers.eq(currentSone))).thenReturn(object : Predicate<Post> {
			override fun apply(input: Post?) = true
		})
	}

	private fun createPost(time: Long, directed: Boolean = false) = mock<Post>().apply {
		whenever(this.time).thenReturn(time)
		whenever(recipient).thenReturn(fromNullable(if (directed) currentSone else null))
	}

	@Test
	fun `index page shows all posts of current sone`() {
		val posts = listOf(createPost(3000), createPost(2000), createPost(1000))
		whenever(currentSone.posts).thenReturn(posts)
		request("", GET)
		page.handleRequest(freenetRequest, templateContext)
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
		request("", GET)
		page.handleRequest(freenetRequest, templateContext)
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
		request("", GET)
		page.handleRequest(freenetRequest, templateContext)
		@Suppress("UNCHECKED_CAST")
		assertThat(templateContext["posts"] as Iterable<Post>, contains(
				posts[0], followedPosts[0], posts[1], followedPosts[1], posts[2]
		))
	}

}
