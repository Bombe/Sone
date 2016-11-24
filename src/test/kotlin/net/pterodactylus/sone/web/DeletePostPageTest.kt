package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.WebTestUtils.redirectsTo
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import kotlin.test.fail

/**
 * Unit test for [DeletePostPage].
 */
class DeletePostPageTest : WebPageTest() {

	private val page = DeletePostPage(template, webInterface)

	private val post = mock<Post>()
	private val sone = mock<Sone>()

	@Before
	fun setupPost() {
		whenever(post.sone).thenReturn(sone)
		whenever(sone.isLocal).thenReturn(true)
	}

	@Test
	fun `get request with invalid post redirects to no permission page`() {
		request("", GET)
		expectedException.expect(redirectsTo("noPermission.html"))
		page.processTemplate(freenetRequest, templateContext)
	}

	@Test
	fun `get request with valid post sets post and return page in template context`() {
		request("", GET)
		addPost("post-id", post)
		addHttpRequestParameter("post", "post-id")
		addHttpRequestParameter("returnPage", "return.html")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["post"], equalTo<Any>(post))
		assertThat(templateContext["returnPage"], equalTo<Any>("return.html"))
	}

	@Test
	fun `post request with invalid post redirects to no permission page`() {
		request("", POST)
		expectedException.expect(redirectsTo("noPermission.html"))
		page.processTemplate(freenetRequest, templateContext)
	}

	@Test
	fun `post request with post from non-local sone redirects to no permission page`() {
		request("", POST)
		whenever(sone.isLocal).thenReturn(false)
		addPost("post-id", post)
		addHttpRequestParameter("post", "post-id")
		addHttpRequestParameter("returnPage", "return.html")
		expectedException.expect(redirectsTo("noPermission.html"))
		page.processTemplate(freenetRequest, templateContext)
	}

	@Test
	fun `post request with confirmation deletes post and redirects to return page`() {
		request("", POST)
		addPost("post-id", post)
		addHttpRequestParameter("post", "post-id")
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("confirmDelete", "true")
		expectedException.expect(redirectsTo("return.html"))
		try {
			page.processTemplate(freenetRequest, templateContext)
			fail()
		} catch (e: Exception) {
			verify(core).deletePost(post)
			throw e
		}
	}

	@Test
	fun `post request with abort delete does not delete post and redirects to return page`() {
		request("", POST)
		addPost("post-id", post)
		addHttpRequestParameter("post", "post-id")
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("abortDelete", "true")
		expectedException.expect(redirectsTo("return.html"))
		try {
			page.processTemplate(freenetRequest, templateContext)
			fail()
		} catch (e: Exception) {
			verify(core, never()).deletePost(post)
			throw e
		}
	}

	@Test
	fun `post request without delete or abort sets post in template context`() {
		request("", POST)
		addPost("post-id", post)
		addHttpRequestParameter("post", "post-id")
		addHttpRequestParameter("returnPage", "return.html")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["post"], equalTo<Any>(post))
		assertThat(templateContext["returnPage"], equalTo<Any>("return.html"))
	}

}
