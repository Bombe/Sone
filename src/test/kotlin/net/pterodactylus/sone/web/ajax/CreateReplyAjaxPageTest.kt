package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [CreateReplyAjaxPage].
 */
class CreateReplyAjaxPageTest : JsonPageTest(::CreateReplyAjaxPage) {

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("createReply.ajax"))
	}

	@Test
	fun `page needs form password`() {
		assertThat(page.needsFormPassword(), equalTo(true))
	}

	@Test
	fun `page requires login`() {
		assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `invalid post ID results in error message`() {
	    assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-post-id"))
	}

	@Test
	fun `valid post ID results in created reply`() {
		val post = mock<Post>()
		addPost("post-id", post)
		val reply = mock<PostReply>().apply { whenever(id).thenReturn("reply-id") }
		whenever(core.createReply(currentSone, post, "")).thenReturn(reply)
	    addRequestParameter("post", "post-id")
		assertThat(json.isSuccess, equalTo(true))
		assertThat(json["reply"].asText(), equalTo("reply-id"))
		assertThat(json["sone"].asText(), equalTo("soneId"))
	}

	@Test
	fun `text is filtered when creating reply`() {
		val post = mock<Post>()
		addPost("post-id", post)
		val reply = mock<PostReply>().apply { whenever(id).thenReturn("reply-id") }
		whenever(core.createReply(currentSone, post, "Text with KSK@foo.bar link")).thenReturn(reply)
		addRequestParameter("post", "post-id")
		addRequestParameter("text", "Text with http://127.0.0.1:8888/KSK@foo.bar link")
		addRequestHeader("Host", "127.0.0.1:8888")
		assertThat(json.isSuccess, equalTo(true))
		assertThat(json["reply"].asText(), equalTo("reply-id"))
		assertThat(json["sone"].asText(), equalTo("soneId"))
	}

	@Test
	fun `sender can be chosen from local sones`() {
	    val sone = mock<Sone>().apply { whenever(id).thenReturn("local-sone") }
		addLocalSone("local-sone", sone)
		val post = mock<Post>()
		addPost("post-id", post)
		val reply = mock<PostReply>().apply { whenever(id).thenReturn("reply-id") }
		whenever(core.createReply(sone, post, "Text")).thenReturn(reply)
		addRequestParameter("post", "post-id")
		addRequestParameter("text", "Text")
		addRequestParameter("sender", "local-sone")
		assertThat(json.isSuccess, equalTo(true))
		assertThat(json["reply"].asText(), equalTo("reply-id"))
		assertThat(json["sone"].asText(), equalTo("local-sone"))
	}

}
