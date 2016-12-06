package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [MarkAsKnownPage].
 */
class MarkAsKnownPageTest : WebPageTest() {

	private val page = MarkAsKnownPage(template, webInterface)

	override fun getPage() = page

	@Test
	fun `posts can be marked as known`() {
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("type", "post")
		addHttpRequestParameter("id", "post1 post2 post3")
		val posts = listOf(mock<Post>(), mock<Post>())
		addPost("post1", posts[0])
		addPost("post3", posts[1])
		verifyRedirect("return.html") {
			verify(core).markPostKnown(posts[0])
			verify(core).markPostKnown(posts[1])
		}
	}

	@Test
	fun `replies can be marked as known`() {
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("type", "reply")
		addHttpRequestParameter("id", "reply1 reply2 reply3")
		val replies = listOf(mock<PostReply>(), mock<PostReply>())
		addPostReply("reply1", replies[0])
		addPostReply("reply3", replies[1])
		verifyRedirect("return.html") {
			verify(core).markReplyKnown(replies[0])
			verify(core).markReplyKnown(replies[1])
		}
	}

	@Test
	fun `sones can be marked as known`() {
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("type", "sone")
		addHttpRequestParameter("id", "sone1 sone2 sone3")
		val sones = listOf(mock<Sone>(), mock<Sone>())
		addSone("sone1", sones[0])
		addSone("sone3", sones[1])
		verifyRedirect("return.html") {
			verify(core).markSoneKnown(sones[0])
			verify(core).markSoneKnown(sones[1])
		}
	}

	@Test
	fun `different type redirects to invalid page`() {
		addHttpRequestParameter("type", "foo")
		verifyRedirect("invalid.html")
	}

}
