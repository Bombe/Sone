package net.pterodactylus.sone.web.ajax

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [LikeAjaxPage].
 */
class LikeAjaxPageTest : JsonPageTest("like.ajax", pageSupplier = ::LikeAjaxPage) {

	@Test
	fun `request with invalid type results in invalid-type error`() {
	    addRequestParameter("type", "invalid")
		addRequestParameter("invalid", "invalid-id")
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-type"))
	}

	@Test
	fun `request with post id results in post being liked by current sone`() {
		addRequestParameter("type", "post")
		addRequestParameter("post", "post-id")
		assertThat(json.isSuccess, equalTo(true))
		verify(currentSone).addLikedPostId("post-id")
		verify(core).touchConfiguration()
	}

	@Test
	fun `request with reply id results in reply being liked by current sone`() {
		addRequestParameter("type", "reply")
		addRequestParameter("reply", "reply-id")
		assertThat(json.isSuccess, equalTo(true))
		verify(currentSone).addLikedReplyId("reply-id")
		verify(core).touchConfiguration()
	}

}
