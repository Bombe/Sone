package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [DeleteReplyAjaxPage].
 */
class DeleteReplyAjaxPageTest : JsonPageTest("deleteReply.ajax", pageSupplier = ::DeleteReplyAjaxPage) {

	@Test
	fun `request with missing reply results in invalid id`() {
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-reply-id"))
	}

	@Test
	fun `request with non-local reply id results in not authorized`() {
		val reply = mock<PostReply>()
		val sone = mock<Sone>()
		whenever(reply.sone).thenReturn(sone)
		addReply(reply, "reply-id")
		addRequestParameter("reply", "reply-id")
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("not-authorized"))
	}

	@Test
	fun `request with local reply id deletes reply`() {
		val reply = mock<PostReply>()
		val sone = mock<Sone>()
		whenever(sone.isLocal).thenReturn(true)
		whenever(reply.sone).thenReturn(sone)
		addReply(reply, "reply-id")
		addRequestParameter("reply", "reply-id")
		assertThat(json.isSuccess, equalTo(true))
		verify(core).deleteReply(reply)
	}

}
