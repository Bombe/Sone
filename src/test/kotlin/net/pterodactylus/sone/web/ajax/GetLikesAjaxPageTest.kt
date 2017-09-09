package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [GetLikesAjaxPage].
 */
class GetLikesAjaxPageTest : JsonPageTest("getLikes.ajax", needsFormPassword = false, pageSupplier = ::GetLikesAjaxPage) {

	@Test
	fun `request without parameters results in failing request`() {
		assertThat(json.isSuccess, equalTo(false))
	}

	@Test
	fun `request with invalid post id results in invalid-post-id`() {
		addRequestParameter("type", "post")
		addRequestParameter("post", "invalid")
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-post-id"))
	}

	@Test
	fun `request with missing post id results in invalid-post-id`() {
		addRequestParameter("type", "post")
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-post-id"))
	}

	@Test
	fun `request with valid post id results in likes for post`() {
		val post = mock<Post>().apply { whenever(id).thenReturn("post-id") }
		addPost(post)
		addLikes(post, createSone(2), createSone(1), createSone(3))
		addRequestParameter("type", "post")
		addRequestParameter("post", "post-id")
		assertThat(json.isSuccess, equalTo(true))
		assertThat(json["likes"].asInt(), equalTo(3))
		assertThat(json["sones"].toList().map { it["id"].asText() to it["name"].asText() }, contains(
				"S1" to "F1 M1 L1",
				"S2" to "F2 M2 L2",
				"S3" to "F3 M3 L3"
		))
	}

	@Test
	fun `request with invalid reply id results in invalid-reply-id`() {
		addRequestParameter("type", "reply")
		addRequestParameter("reply", "invalid")
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-reply-id"))
	}

	@Test
	fun `request with missing reply id results in invalid-reply-id`() {
		addRequestParameter("type", "reply")
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-reply-id"))
	}

	@Test
	fun `request with valid reply id results in likes for reply`() {
		val reply = mock<PostReply>().apply { whenever(id).thenReturn("reply-id") }
		addReply(reply)
		addLikes(reply, createSone(2), createSone(1), createSone(3))
		addRequestParameter("type", "reply")
		addRequestParameter("reply", "reply-id")
		assertThat(json.isSuccess, equalTo(true))
		assertThat(json["likes"].asInt(), equalTo(3))
		assertThat(json["sones"].toList().map { it["id"].asText() to it["name"].asText() }, contains(
				"S1" to "F1 M1 L1",
				"S2" to "F2 M2 L2",
				"S3" to "F3 M3 L3"
		))
	}

	@Test
	fun `request with invalid type results in invalid-type`() {
		addRequestParameter("type", "invalid")
		addRequestParameter("invalid", "foo")
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-type"))
	}

}

private fun createSone(index: Int) = mock<Sone>().apply {
	whenever(id).thenReturn("S$index")
	whenever(profile).thenReturn(Profile(this).apply {
		firstName = "F$index"
		middleName = "M$index"
		lastName = "L$index"
	})
}
