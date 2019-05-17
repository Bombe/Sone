package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.baseInjector
import net.pterodactylus.sone.web.page.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import java.net.*

/**
 * Unit test for [ViewPostPage].
 */
class ViewPostPageTest: WebPageTest(::ViewPostPage) {

	private val post = mock<Post>()

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("viewPost.html"))
	}

	@Test
	fun `page does not require login`() {
		assertThat(page.requiresLogin(), equalTo(false))
	}

	@Test
	fun `the view post page is link-excepted`() {
		assertThat(page.isLinkExcepted(URI("")), equalTo(true))
	}

	@Test
	fun `get request without parameters stores null in template context`() {
		verifyNoRedirect {
			assertThat(templateContext["post"], nullValue())
			assertThat(templateContext["raw"] as? Boolean, equalTo(false))
		}
	}

	@Test
	fun `get request with invalid post id stores null in template context`() {
		addHttpRequestParameter("post", "invalid-post-id")
		verifyNoRedirect {
			assertThat(templateContext["post"], nullValue())
			assertThat(templateContext["raw"] as? Boolean, equalTo(false))
		}
	}

	@Test
	fun `get request with valid post id stores post in template context`() {
		addPost("post-id", post)
		addHttpRequestParameter("post", "post-id")
		verifyNoRedirect {
			assertThat(templateContext["post"], equalTo<Any>(post))
			assertThat(templateContext["raw"] as? Boolean, equalTo(false))
		}
	}

	@Test
	fun `get request with valid post id and raw=true stores post in template context`() {
		addPost("post-id", post)
		addHttpRequestParameter("post", "post-id")
		addHttpRequestParameter("raw", "true")
		verifyNoRedirect {
			assertThat(templateContext["post"], equalTo<Any>(post))
			assertThat(templateContext["raw"] as? Boolean, equalTo(true))
		}
	}

	@Test
	fun `page title for request without parameters is default title`() {
		addTranslation("Page.ViewPost.Title", "view post title")
		assertThat(page.getPageTitle(soneRequest), equalTo("view post title"))
	}

	@Test
	fun `page title for request with invalid post is default title`() {
		addHttpRequestParameter("post", "invalid-post-id")
		addTranslation("Page.ViewPost.Title", "view post title")
		assertThat(page.getPageTitle(soneRequest), equalTo("view post title"))
	}

	@Test
	fun `page title for request with valid post is first twenty chars of post plus sone name plus default title`() {
		whenever(currentSone.profile).thenReturn(Profile(currentSone).apply {
			firstName = "First"
			middleName = "M."
			lastName = "Last"
		})
		whenever(post.sone).thenReturn(currentSone)
		whenever(post.text).thenReturn("This is a text that is longer than twenty characters.")
		addPost("post-id", post)
		addHttpRequestParameter("post", "post-id")
		addTranslation("Page.ViewPost.Title", "view post title")
		assertThat(page.getPageTitle(soneRequest), equalTo("This is a text that … - First M. Last - view post title"))
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<ViewPostPage>(), notNullValue())
	}

	@Test
	fun `page is annotated with correct template path`() {
	    assertThat(page.templatePath, equalTo("/templates/viewPost.html"))
	}

}
