package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.web.baseInjector
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [MarkAsKnownPage].
 */
class MarkAsKnownPageTest: WebPageTest(::MarkAsKnownPage) {

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("markAsKnown.html"))
	}

	@Test
	fun `page does not require login`() {
		assertThat(page.requiresLogin(), equalTo(false))
	}

	@Test
	fun `page returns correct title`() {
		addTranslation("Page.MarkAsKnown.Title", "mark as known page title")
		assertThat(page.getPageTitle(freenetRequest), equalTo("mark as known page title"))
	}

	@Test
	fun `posts can be marked as known`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("type", "post")
		addHttpRequestPart("id", "post1 post2 post3")
		val posts = listOf(mock<Post>(), mock())
		addPost("post1", posts[0])
		addPost("post3", posts[1])
		verifyRedirect("return.html") {
			verify(core).markPostKnown(posts[0])
			verify(core).markPostKnown(posts[1])
		}
	}

	@Test
	fun `replies can be marked as known`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("type", "reply")
		addHttpRequestPart("id", "reply1 reply2 reply3")
		val replies = listOf(mock<PostReply>(), mock())
		addPostReply("reply1", replies[0])
		addPostReply("reply3", replies[1])
		verifyRedirect("return.html") {
			verify(core).markReplyKnown(replies[0])
			verify(core).markReplyKnown(replies[1])
		}
	}

	@Test
	fun `sones can be marked as known`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("type", "sone")
		addHttpRequestPart("id", "sone1 sone2 sone3")
		val sones = listOf(mock<Sone>(), mock())
		addSone("sone1", sones[0])
		addSone("sone3", sones[1])
		verifyRedirect("return.html") {
			verify(core).markSoneKnown(sones[0])
			verify(core).markSoneKnown(sones[1])
		}
	}

	@Test
	fun `different type redirects to invalid page`() {
		setMethod(POST)
		addHttpRequestPart("type", "foo")
		verifyRedirect("invalid.html")
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<MarkAsKnownPage>(), notNullValue())
	}

}
