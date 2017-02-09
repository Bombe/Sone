package net.pterodactylus.sone.web

import com.google.common.base.Optional.absent
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.asOptional
import net.pterodactylus.sone.test.mock
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [CreatePostPage].
 */
class CreatePostPageTest: WebPageTest() {

	private val page = CreatePostPage(template, webInterface)

	override fun getPage() = page

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("createPost.html"))
	}

	@Test
	fun `page requires login`() {
		assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `return page is set in template context`() {
		addHttpRequestParameter("returnPage", "return.html")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["returnPage"], equalTo<Any>("return.html"))
	}

	@Test
	fun `post is created correctly`() {
		request("", POST)
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("text", "post text")
		verifyRedirect("return.html") {
			verify(core).createPost(currentSone, absent(), "post text")
		}
	}

	@Test
	fun `creating an empty post is denied`() {
		request("", POST)
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("text", "  ")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["errorTextEmpty"], equalTo<Any>(true))
	}

	@Test
	fun `a sender can be selected`() {
		request("", POST)
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("text", "post text")
		addHttpRequestParameter("sender", "sender-id")
		val sender = mock<Sone>()
		addLocalSone("sender-id", sender)
		verifyRedirect("return.html") {
			verify(core).createPost(sender, absent(), "post text")
		}
	}

	@Test
	fun `a recipient can be selected`() {
		request("", POST)
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("text", "post text")
		addHttpRequestParameter("recipient", "recipient-id")
		val recipient = mock<Sone>()
		addSone("recipient-id", recipient)
		verifyRedirect("return.html") {
			verify(core).createPost(currentSone, recipient.asOptional(), "post text")
		}
	}

	@Test
	fun `text is filtered correctly`() {
		request("", POST)
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("text", "post http://localhost:12345/KSK@foo text")
		addHttpRequestHeader("Host", "localhost:12345")
		verifyRedirect("return.html") {
			verify(core).createPost(currentSone, absent(), "post KSK@foo text")
		}
	}

}
