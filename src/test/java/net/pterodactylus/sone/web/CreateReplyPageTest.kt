package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [CreateReplyPage].
 */
class CreateReplyPageTest: WebPageTest() {

	private val page = CreateReplyPage(template, webInterface)
	override fun getPage() = page

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("createReply.html"))
	}

	@Test
	fun `page requires login`() {
		assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `reply is created correctly`() {
		request("", POST)
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("post", "post-id")
		addHttpRequestParameter("text", "new text")
		val post = mock<Post>().apply { addPost("post-id", this) }
		verifyRedirect("return.html") {
			verify(core).createReply(currentSone, post, "new text")
		}
	}

	@Test
	fun `reply is filtered`() {
		request("", POST)
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("post", "post-id")
		addHttpRequestParameter("text", "new http://localhost:12345/KSK@foo text")
		addHttpRequestHeader("Host", "localhost:12345")
		val post = mock<Post>().apply { addPost("post-id", this) }
		verifyRedirect("return.html") {
			verify(core).createReply(currentSone, post, "new KSK@foo text")
		}
	}

	@Test
	fun `reply is created with correct sender`() {
		request("", POST)
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("post", "post-id")
		addHttpRequestParameter("text", "new text")
		addHttpRequestParameter("sender", "sender-id")
		val sender = mock<Sone>().apply { addLocalSone("sender-id", this) }
		val post = mock<Post>().apply { addPost("post-id", this) }
		verifyRedirect("return.html") {
			verify(core).createReply(sender, post, "new text")
		}
	}

	@Test
	fun `empty text sets parameters in template contexty`() {
		request("", POST)
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("post", "post-id")
		addHttpRequestParameter("text", "  ")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["errorTextEmpty"], equalTo<Any>(true))
		assertThat(templateContext["returnPage"], equalTo<Any>("return.html"))
		assertThat(templateContext["postId"], equalTo<Any>("post-id"))
		assertThat(templateContext["text"], equalTo<Any>(""))
	}

	@Test
	fun `user is redirected to no permissions page if post does not exist`() {
		request("", POST)
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("post", "post-id")
		addHttpRequestParameter("text", "new text")
		verifyRedirect("noPermission.html")
	}

	@Test
	fun `get request stores parameters in template context`() {
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("post", "post-id")
		addHttpRequestParameter("text", "new text")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["returnPage"], equalTo<Any>("return.html"))
		assertThat(templateContext["postId"], equalTo<Any>("post-id"))
		assertThat(templateContext["text"], equalTo<Any>("new text"))
	}

}
