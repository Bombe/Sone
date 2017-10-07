package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [LikeAjaxPage].
 */
class LikeAjaxPageTest : JsonPageTest("like.ajax", pageSupplier = ::LikeAjaxPage) {

	@Test
	fun `request with invalid type results in invalid-type error`() {
		addRequestParameter("type", "invalid")
		addRequestParameter("invalid", "invalid-id")
		assertThatJsonFailed("invalid-type")
	}

	@Test
	fun `request with valid post id results in post being liked by current sone`() {
		addRequestParameter("type", "post")
		addRequestParameter("post", "post-id")
		addPost(mock<Post>().apply { whenever(id).thenReturn("post-id") })
		assertThatJsonIsSuccessful()
		verify(currentSone).addLikedPostId("post-id")
		verify(core).touchConfiguration()
	}

	@Test
	fun `request with valid reply id results in reply being liked by current sone`() {
		addRequestParameter("type", "reply")
		addRequestParameter("reply", "reply-id")
		addReply(mock<PostReply>().apply { whenever(id).thenReturn("reply-id") })
		assertThatJsonIsSuccessful()
		verify(currentSone).addLikedReplyId("reply-id")
		verify(core).touchConfiguration()
	}

	@Test
	fun `request with invalid post id results in post being liked by current sone`() {
		addRequestParameter("type", "post")
		addRequestParameter("post", "post-id")
		assertThat(json.isSuccess, equalTo(false))
		verify(currentSone, never()).addLikedPostId("post-id")
		verify(core, never()).touchConfiguration()
	}

	@Test
	fun `request with invalid reply id results in reply being liked by current sone`() {
		addRequestParameter("type", "reply")
		addRequestParameter("reply", "reply-id")
		assertThat(json.isSuccess, equalTo(false))
		verify(currentSone, never()).addLikedReplyId("reply-id")
		verify(core, never()).touchConfiguration()
	}

}
