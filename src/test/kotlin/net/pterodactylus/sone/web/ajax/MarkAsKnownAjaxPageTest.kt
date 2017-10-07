package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [MarkAsKnownAjaxPage].
 */
class MarkAsKnownAjaxPageTest : JsonPageTest("markAsKnown.ajax", requiresLogin = false, pageSupplier = ::MarkAsKnownAjaxPage) {

	@Test
	fun `request without type results in invalid-type`() {
		assertThatJsonFailed("invalid-type")
	}

	@Test
	fun `request with unknown sone returns successfully`() {
		addRequestParameter("type", "sone")
		addRequestParameter("id", "invalid")
		assertThatJsonIsSuccessful()
		verify(core, never()).markSoneKnown(any())
	}

	@Test
	fun `request with multiple valid sones marks sones as known and returns successfully`() {
		addRequestParameter("type", "sone")
		addRequestParameter("id", "sone-id1 sone-id2")
		val sone1 = mock<Sone>().apply { whenever(id).thenReturn("sone-id1") }
		val sone2 = mock<Sone>().apply { whenever(id).thenReturn("sone-id2") }
		addSone(sone1)
		addSone(sone2)
		assertThatJsonIsSuccessful()
		verify(core).markSoneKnown(sone1)
		verify(core).markSoneKnown(sone2)
	}

	@Test
	fun `request with multiple valid posts marks posts as known and returns successfully`() {
		addRequestParameter("type", "post")
		addRequestParameter("id", "post1 post2 post 3")
		val post1 = mock<Post>()
		val post2 = mock<Post>()
		addPost(post1, "post1")
		addPost(post2, "post2")
		assertThatJsonIsSuccessful()
		verify(core).markPostKnown(post1)
		verify(core).markPostKnown(post2)
	}

	@Test
	fun `request with multiple valid replies marks replies as known and returns successfully`() {
		addRequestParameter("type", "reply")
		addRequestParameter("id", "reply1 reply2 reply3")
		val reply1 = mock<PostReply>()
		val reply2 = mock<PostReply>()
		addReply(reply1, "reply1")
		addReply(reply2, "reply2")
		assertThatJsonIsSuccessful()
		verify(core).markReplyKnown(reply1)
		verify(core).markReplyKnown(reply2)
	}

}
