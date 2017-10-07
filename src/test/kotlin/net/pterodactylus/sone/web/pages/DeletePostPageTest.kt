package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [DeletePostPage].
 */
class DeletePostPageTest: WebPageTest(::DeletePostPage) {

	private val post = mock<Post>()
	private val sone = mock<Sone>()

	@Before
	fun setupPost() {
		whenever(post.sone).thenReturn(sone)
		whenever(sone.isLocal).thenReturn(true)
	}

	@Test
	fun `page returns correct path`() {
	    assertThat(page.path, equalTo("deletePost.html"))
	}

	@Test
	fun `page requires login`() {
	    assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `get request with invalid post redirects to no permission page`() {
		verifyRedirect("noPermission.html")
	}

	@Test
	fun `get request with valid post sets post and return page in template context`() {
		addPost("post-id", post)
		addHttpRequestParameter("post", "post-id")
		addHttpRequestParameter("returnPage", "return.html")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["post"], equalTo<Any>(post))
		assertThat(templateContext["returnPage"], equalTo<Any>("return.html"))
	}

	@Test
	fun `post request with invalid post redirects to no permission page`() {
		setMethod(POST)
		verifyRedirect("noPermission.html")
	}

	@Test
	fun `post request with post from non-local sone redirects to no permission page`() {
		setMethod(POST)
		whenever(sone.isLocal).thenReturn(false)
		addPost("post-id", post)
		addHttpRequestPart("post", "post-id")
		addHttpRequestPart("returnPage", "return.html")
		verifyRedirect("noPermission.html")
	}

	@Test
	fun `post request with confirmation deletes post and redirects to return page`() {
		setMethod(POST)
		addPost("post-id", post)
		addHttpRequestPart("post", "post-id")
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("confirmDelete", "true")
		verifyRedirect("return.html") {
			verify(core).deletePost(post)
		}
	}

	@Test
	fun `post request with abort delete does not delete post and redirects to return page`() {
		setMethod(POST)
		addPost("post-id", post)
		addHttpRequestPart("post", "post-id")
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("abortDelete", "true")
		verifyRedirect("return.html") {
			verify(core, never()).deletePost(post)
		}
	}

	@Test
	fun `post request without delete or abort sets post in template context`() {
		setMethod(POST)
		addPost("post-id", post)
		addHttpRequestPart("post", "post-id")
		addHttpRequestPart("returnPage", "return.html")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["post"], equalTo<Any>(post))
		assertThat(templateContext["returnPage"], equalTo<Any>("return.html"))
	}

}
