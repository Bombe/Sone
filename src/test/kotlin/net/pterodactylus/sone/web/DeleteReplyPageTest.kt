package net.pterodactylus.sone.web

import net.pterodactylus.sone.web.WebTestUtils.redirectsTo
import net.pterodactylus.util.web.Method.POST
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.`when`

import net.pterodactylus.sone.data.PostReply

import com.google.common.base.Optional
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method.GET
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [DeleteReplyPage].
 */
class DeleteReplyPageTest : WebPageTest() {

	private val page = DeleteReplyPage(template, webInterface)

	private val sone = mock<Sone>()
	private val reply = mock<PostReply>()

	@Before
	fun setupReply() {
		whenever(sone.isLocal).thenReturn(true)
		whenever(reply.sone).thenReturn(sone)
	}
	
	@Test
	fun `get request sets reply ID and return page in template context`() {
	    request("", GET)
		addHttpRequestParameter("reply", "reply-id")
		addHttpRequestParameter("returnPage", "return.html")
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["reply"], equalTo<Any>("reply-id"))
		assertThat(templateContext["returnPage"], equalTo<Any>("return.html"))
	}

	@Test
	fun `post request without any action sets reply ID and return page in template context`() {
	    request("", POST)
		addPostReply("reply-id", reply)
		addHttpRequestParameter("reply", "reply-id")
		addHttpRequestParameter("returnPage", "return.html")
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["reply"], equalTo<Any>("reply-id"))
		assertThat(templateContext["returnPage"], equalTo<Any>("return.html"))
	}

	@Test
	fun `trying to delete a reply with an invalid ID results in no permission page`() {
		request("", POST)
		expectedException.expect(redirectsTo("noPermission.html"))
		page.processTemplate(freenetRequest, templateContext)
	}

	@Test
	fun `trying to delete a reply from a non-local sone results in no permission page`() {
	    request("", POST)
		addHttpRequestParameter("reply", "reply-id")
		whenever(sone.isLocal).thenReturn(false)
		addPostReply("reply-id", reply)
		expectedException.expect(redirectsTo("noPermission.html"))
		page.processTemplate(freenetRequest, templateContext)
	}

	@Test
	fun `confirming deletion of reply deletes the reply and redirects to return page`() {
	    request("", POST)
		addPostReply("reply-id", reply)
		addHttpRequestParameter("reply", "reply-id")
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("confirmDelete", "true")
		expectedException.expect(redirectsTo("return.html"))
		try {
			page.processTemplate(freenetRequest, templateContext)
		} finally {
			verify(core).deleteReply(reply)
		}
	}
	
	@Test
	fun `aborting deletion of reply redirects to return page`() {
	    request("", POST)
		addPostReply("reply-id", reply)
		addHttpRequestParameter("reply", "reply-id")
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("abortDelete", "true")
		expectedException.expect(redirectsTo("return.html"))
		try {
			page.processTemplate(freenetRequest, templateContext)
		} finally {
			verify(core, never()).deleteReply(reply)
		}
	}

}
