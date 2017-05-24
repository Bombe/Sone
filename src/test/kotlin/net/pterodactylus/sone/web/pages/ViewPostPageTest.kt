package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test

/**
 * Unit test for [ViewPostPage].
 */
class ViewPostPageTest : WebPageTest() {

	private val page = ViewPostPage(template, webInterface)
	private val post = mock<Post>()

	override fun getPage() = page

	@Test
	fun `the view post page is link-excepted`() {
		assertThat(page.isLinkExcepted(null), equalTo(true))
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
		assertThat(page.getPageTitle(freenetRequest), equalTo("Page.ViewPost.Title"))
	}

	@Test
	fun `page title for request with invalid post is default title`() {
		addHttpRequestParameter("post", "invalid-post-id")
		assertThat(page.getPageTitle(freenetRequest), equalTo("Page.ViewPost.Title"))
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
		assertThat(page.getPageTitle(freenetRequest), equalTo("This is a text that … - First M. Last - Page.ViewPost.Title"))
	}

}
