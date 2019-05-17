package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [DeleteReplyPage].
 */
class DeleteReplyPageTest : WebPageTest(::DeleteReplyPage) {

	private val sone = mock<Sone>()
	private val reply = mock<PostReply>()

	@Before
	fun setupReply() {
		whenever(sone.isLocal).thenReturn(true)
		whenever(reply.sone).thenReturn(sone)
	}

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("deleteReply.html"))
	}

	@Test
	fun `page requires login`() {
		assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `get request sets reply ID and return page in template context`() {
		addHttpRequestParameter("reply", "reply-id")
		addHttpRequestParameter("returnPage", "return.html")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["reply"], equalTo<Any>("reply-id"))
		assertThat(templateContext["returnPage"], equalTo<Any>("return.html"))
	}

	@Test
	fun `post request without any action sets reply ID and return page in template context`() {
		setMethod(POST)
		addPostReply("reply-id", reply)
		addHttpRequestPart("reply", "reply-id")
		addHttpRequestPart("returnPage", "return.html")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["reply"], equalTo<Any>("reply-id"))
		assertThat(templateContext["returnPage"], equalTo<Any>("return.html"))
	}

	@Test
	fun `trying to delete a reply with an invalid ID results in no permission page`() {
		setMethod(POST)
		verifyRedirect("noPermission.html")
	}

	@Test
	fun `trying to delete a reply from a non-local sone results in no permission page`() {
		setMethod(POST)
		addHttpRequestPart("reply", "reply-id")
		whenever(sone.isLocal).thenReturn(false)
		addPostReply("reply-id", reply)
		verifyRedirect("noPermission.html")
	}

	@Test
	fun `confirming deletion of reply deletes the reply and redirects to return page`() {
		setMethod(POST)
		addPostReply("reply-id", reply)
		addHttpRequestPart("reply", "reply-id")
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("confirmDelete", "true")
		verifyRedirect("return.html") {
			verify(core).deleteReply(reply)
		}
	}

	@Test
	fun `aborting deletion of reply redirects to return page`() {
		setMethod(POST)
		addPostReply("reply-id", reply)
		addHttpRequestPart("reply", "reply-id")
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("abortDelete", "true")
		verifyRedirect("return.html") {
			verify(core, never()).deleteReply(reply)
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<DeleteReplyPage>(), notNullValue())
	}

	@Test
	fun `page is annotated with correct template path`() {
		assertThat(page.templatePath, equalTo("/templates/deleteReply.html"))
	}

}
