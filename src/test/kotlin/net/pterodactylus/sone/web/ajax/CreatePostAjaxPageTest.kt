package net.pterodactylus.sone.web.ajax

import com.google.common.base.Optional
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.utils.asOptional
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test

/**
 * Unit test for [CreatePostAjaxPage].
 */
class CreatePostAjaxPageTest : JsonPageTest("createPost.ajax", pageSupplier = ::CreatePostAjaxPage) {

	@Test
	fun `missing text parameter returns error`() {
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("text-required"))
	}

	@Test
	fun `empty text returns error`() {
		addRequestParameter("text", "")
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("text-required"))
	}

	@Test
	fun `whitespace-only text returns error`() {
		addRequestParameter("text", "  ")
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("text-required"))
	}

	@Test
	fun `request with valid data creates post`() {
		addRequestParameter("text", "test")
		val post = createPost()
		whenever(core.createPost(currentSone, Optional.absent(), "test")).thenReturn(post)
		assertThat(json.isSuccess, equalTo(true))
		assertThat(json["postId"].asText(), equalTo("id"))
		assertThat(json["sone"].asText(), equalTo(currentSone.id))
		assertThat(json["recipient"], nullValue())
	}

	@Test
	fun `request with invalid recipient creates post without recipient`() {
		addRequestParameter("text", "test")
		addRequestParameter("recipient", "invalid")
		val post = createPost()
		whenever(core.createPost(currentSone, Optional.absent(), "test")).thenReturn(post)
		assertThat(json.isSuccess, equalTo(true))
		assertThat(json["postId"].asText(), equalTo("id"))
		assertThat(json["sone"].asText(), equalTo(currentSone.id))
		assertThat(json["recipient"], nullValue())
	}

	@Test
	fun `request with valid data and recipient creates correct post`() {
		addRequestParameter("text", "test")
		addRequestParameter("recipient", "valid")
		val recipient = mock<Sone>().apply { whenever(id).thenReturn("valid") }
		addSone(recipient)
		val post = createPost("valid")
		whenever(core.createPost(currentSone, Optional.of(recipient), "test")).thenReturn(post)
		assertThat(json.isSuccess, equalTo(true))
		assertThat(json["postId"].asText(), equalTo("id"))
		assertThat(json["sone"].asText(), equalTo(currentSone.id))
		assertThat(json["recipient"].asText(), equalTo("valid"))
	}

	@Test
	fun `text is filtered correctly`() {
		addRequestParameter("text", "Link http://freenet.test:8888/KSK@foo is filtered")
		addRequestHeader("Host", "freenet.test:8888")
		val post = createPost()
		whenever(core.createPost(currentSone, Optional.absent(), "Link KSK@foo is filtered")).thenReturn(post)
		assertThat(json.isSuccess, equalTo(true))
		assertThat(json["postId"].asText(), equalTo("id"))
		assertThat(json["sone"].asText(), equalTo(currentSone.id))
		assertThat(json["recipient"], nullValue())
	}

	private fun createPost(recipientId: String? = null) =
			mock<Post>().apply {
				whenever(id).thenReturn("id")
				whenever(sone).thenReturn(currentSone)
				whenever(this.recipientId).thenReturn(recipientId.asOptional())
			}

}
