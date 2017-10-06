package net.pterodactylus.sone.web.ajax

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [UnlikeAjaxPage].
 */
class UnlikeAjaxPageTest : JsonPageTest("unlike.ajax", pageSupplier = ::UnlikeAjaxPage) {

	@Test
	fun `request without type results in error`() {
		assertThat(json.isSuccess, equalTo(false))
	}

	@Test
	fun `request for post without id results in invalid-post-id`() {
		addRequestParameter("type", "post")
		assertThatJsonFailed("invalid-post-id")
	}

	@Test
	fun `request for invalid type results in invalid-type`() {
		addRequestParameter("type", "invalid")
		addRequestParameter("invalid", "invalid")
		assertThatJsonFailed("invalid-type")
	}

	@Test
	fun `request for post with id removes id from liked posts`() {
		addRequestParameter("type", "post")
		addRequestParameter("post", "post-id")
		assertThatJsonIsSuccessful()
		verify(currentSone).removeLikedPostId("post-id")
		verify(core).touchConfiguration()
	}

	@Test
	fun `request for reply without id results in invalid-reply-id`() {
		addRequestParameter("type", "reply")
		assertThatJsonFailed("invalid-reply-id")
	}

	@Test
	fun `request for reply with id removes id from liked replys`() {
		addRequestParameter("type", "reply")
		addRequestParameter("reply", "reply-id")
		assertThatJsonIsSuccessful()
		verify(currentSone).removeLikedReplyId("reply-id")
		verify(core).touchConfiguration()
	}

}
