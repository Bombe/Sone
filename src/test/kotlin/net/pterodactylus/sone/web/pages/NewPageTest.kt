package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import java.util.Arrays.*

/**
 * Unit test for [NewPage].
 */
class NewPageTest : WebPageTest() {

	private val newElements = mock<NewElements>()
	override val page: SoneTemplatePage
		get() = NewPage(webInterface, loaders, templateRenderer, newElements)

	@Before
	fun setupNumberOfPostsPerPage() {
		webInterface.core.preferences.newPostsPerPage = 5
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
		assertThat(page.getPageTitle(soneRequest), equalTo("new page title"))
	}

	@Test
	fun `posts are not duplicated when they come from both new posts and new replies notifications`() {
		val extraPost = mock<Post>().withTime(2000)
		val posts = asList(mock<Post>().withTime(1000), mock<Post>().withTime(3000))
		val postReplies = asList(mock<PostReply>(), mock())
		whenever(postReplies[0].post).thenReturn(posts[0].asOptional())
		whenever(postReplies[1].post).thenReturn(extraPost.asOptional())
		whenever(newElements.newPosts).thenReturn(posts)
		whenever(newElements.newReplies).thenReturn(postReplies)

		verifyNoRedirect {
			val renderedPosts = templateContext.get<List<Post>>("posts", List::class.java)
			assertThat(renderedPosts, containsInAnyOrder(posts[1], extraPost, posts[0]))
		}
	}

	private fun Post.withTime(time: Long) = apply { whenever(this.time).thenReturn(time) }

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `posts are paginated properly`() {
		webInterface.core.preferences.newPostsPerPage = 2
		val posts = listOf(mock<Post>().withTime(2000), mock<Post>().withTime(3000), mock<Post>().withTime(1000))
		whenever(newElements.newPosts).thenReturn(posts)
		verifyNoRedirect {
			assertThat((templateContext["pagination"] as Pagination<Post>).items, contains(posts[1], posts[0]))
		}
	}

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `posts are paginated properly on second page`() {
		webInterface.core.preferences.newPostsPerPage = 2
		addHttpRequestParameter("page", "1")
		val posts = listOf(mock<Post>().withTime(2000), mock<Post>().withTime(3000), mock<Post>().withTime(1000))
		whenever(newElements.newPosts).thenReturn(posts)
		verifyNoRedirect {
			assertThat((templateContext["pagination"] as Pagination<Post>).items, contains(posts[2]))
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<NewPage>(), notNullValue())
	}

	@Test
	fun `page is annotated with the correct menuname`() {
		assertThat(page.menuName, equalTo("New"))
	}

	@Test
	fun `page is annotated with correct template path`() {
		assertThat(page.templatePath, equalTo("/templates/new.html"))
	}

}
