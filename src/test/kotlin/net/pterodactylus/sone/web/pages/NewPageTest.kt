package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.test.asOptional
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import java.util.Arrays.asList

/**
 * Unit test for [NewPage].
 */
class NewPageTest: WebPageTest() {

	private val page = NewPage(template, webInterface)

	override fun getPage() = page

	@Before
	fun setupNumberOfPostsPerPage() {
		webInterface.core.preferences.postsPerPage = 5
	}

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("new.html"))
	}

	@Test
	fun `page does not require login`() {
		assertThat(page.requiresLogin(), equalTo(false))
	}

	@Test
	fun `page returns correct title`() {
		addTranslation("Page.New.Title", "new page title")
		assertThat(page.getPageTitle(freenetRequest), equalTo("new page title"))
	}

	@Test
	fun `posts are not duplicated when they come from both new posts and new replies notifications`() {
		val extraPost = mock<Post>()
		val posts = asList(mock<Post>(), mock<Post>())
		val postReplies = asList(mock<PostReply>(), mock<PostReply>())
		whenever(postReplies[0].post).thenReturn(posts[0].asOptional())
		whenever(postReplies[1].post).thenReturn(extraPost.asOptional())
		whenever(webInterface.getNewPosts(currentSone)).thenReturn(posts)
		whenever(webInterface.getNewReplies(currentSone)).thenReturn(postReplies)

		verifyNoRedirect {
			val renderedPosts = templateContext.get<List<Post>>("posts", List::class.java)
			assertThat(renderedPosts, containsInAnyOrder(posts[0], posts[1], extraPost))
		}
	}

}
