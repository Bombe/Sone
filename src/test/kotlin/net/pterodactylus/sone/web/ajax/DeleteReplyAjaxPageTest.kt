package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [DeleteReplyAjaxPage].
 */
class DeleteReplyAjaxPageTest : JsonPageTest("deleteReply.ajax", pageSupplier = ::DeleteReplyAjaxPage) {

	@Test
	fun `request with missing reply results in invalid id`() {
		assertThatJsonFailed("invalid-reply-id")
	}

	@Test
	fun `request with non-local reply id results in not authorized`() {
		val reply = mock<PostReply>()
		val sone = mock<Sone>()
		whenever(reply.sone).thenReturn(sone)
		addReply(reply, "reply-id")
		addRequestParameter("reply", "reply-id")
		assertThatJsonFailed("not-authorized")
	}

	@Test
	fun `request with local reply id deletes reply`() {
		val reply = mock<PostReply>()
		val sone = mock<Sone>()
		whenever(sone.isLocal).thenReturn(true)
		whenever(reply.sone).thenReturn(sone)
		addReply(reply, "reply-id")
		addRequestParameter("reply", "reply-id")
		assertThatJsonIsSuccessful()
		verify(core).deleteReply(reply)
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<DeleteReplyAjaxPage>(), notNullValue())
	}

}
