package net.pterodactylus.sone.web.pages

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
class CreateReplyPageTest: WebPageTest(::CreateReplyPage) {

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
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("post", "post-id")
		addHttpRequestPart("text", "new text")
		val post = mock<Post>().apply { addPost("post-id", this) }
		verifyRedirect("return.html") {
			verify(core).createReply(currentSone, post, "new text")
		}
	}

	@Test
	fun `reply is filtered`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("post", "post-id")
		addHttpRequestPart("text", "new http://localhost:12345/KSK@foo text")
		addHttpRequestHeader("Host", "localhost:12345")
		val post = mock<Post>().apply { addPost("post-id", this) }
		verifyRedirect("return.html") {
			verify(core).createReply(currentSone, post, "new KSK@foo text")
		}
	}

	@Test
	fun `reply is created with correct sender`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("post", "post-id")
		addHttpRequestPart("text", "new text")
		addHttpRequestPart("sender", "sender-id")
		val sender = mock<Sone>().apply { addLocalSone("sender-id", this) }
		val post = mock<Post>().apply { addPost("post-id", this) }
		verifyRedirect("return.html") {
			verify(core).createReply(sender, post, "new text")
		}
	}

	@Test
	fun `empty text sets parameters in template contexty`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("post", "post-id")
		addHttpRequestPart("text", "  ")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["errorTextEmpty"], equalTo<Any>(true))
		assertThat(templateContext["returnPage"], equalTo<Any>("return.html"))
		assertThat(templateContext["postId"], equalTo<Any>("post-id"))
		assertThat(templateContext["text"], equalTo<Any>(""))
	}

	@Test
	fun `user is redirected to no permissions page if post does not exist`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("post", "post-id")
		addHttpRequestPart("text", "new text")
		verifyRedirect("noPermission.html")
	}

}
