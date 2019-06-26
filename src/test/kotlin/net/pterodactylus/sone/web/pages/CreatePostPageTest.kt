package net.pterodactylus.sone.web.pages

import com.google.common.base.Optional.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [CreatePostPage].
 */
class CreatePostPageTest : WebPageTest(::CreatePostPage) {

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
		addHttpRequestPart("returnPage", "return.html")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["returnPage"], equalTo<Any>("return.html"))
	}

	@Test
	fun `post is created correctly`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("text", "post text")
		verifyRedirect("return.html") {
			verify(core).createPost(currentSone, absent(), "post text")
		}
	}

	@Test
	fun `creating an empty post is denied`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("text", "  ")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["errorTextEmpty"], equalTo<Any>(true))
	}

	@Test
	fun `a sender can be selected`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("text", "post text")
		addHttpRequestPart("sender", "sender-id")
		val sender = mock<Sone>()
		addLocalSone("sender-id", sender)
		verifyRedirect("return.html") {
			verify(core).createPost(sender, absent(), "post text")
		}
	}

	@Test
	fun `a recipient can be selected`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("text", "post text")
		addHttpRequestPart("recipient", "recipient-id")
		val recipient = mock<Sone>()
		addSone("recipient-id", recipient)
		verifyRedirect("return.html") {
			verify(core).createPost(currentSone, recipient.asOptional(), "post text")
		}
	}

	@Test
	fun `text is filtered correctly`() {
		setMethod(POST)
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("text", "post http://localhost:12345/KSK@foo text")
		addHttpRequestHeader("Host", "localhost:12345")
		verifyRedirect("return.html") {
			verify(core).createPost(currentSone, absent(), "post KSK@foo text")
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<CreatePostPage>(), notNullValue())
	}

	@Test
	fun `page is annotated with correct template path`() {
		assertThat(page.templatePath, equalTo("/templates/createPost.html"))
	}

}
