package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.utils.asOptional
import net.pterodactylus.sone.utils.asTemplate
import net.pterodactylus.sone.web.baseInjector
import net.pterodactylus.util.template.ReflectionAccessor
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test

/**
 * Unit test for [GetPostAjaxPage].
 */
class GetPostAjaxPageTest : JsonPageTest("getPost.ajax", needsFormPassword = false,
		pageSupplier = { webInterface ->
			GetPostAjaxPage(webInterface, "<%core>\n<%request>\n<%post.text>\n<%currentSone>\n<%localSones>".asTemplate())
		}) {

	@Test
	fun `request with missing post results in invalid-post-id`() {
		assertThatJsonFailed("invalid-post-id")
	}

	@Test
	fun `request with valid post results in post json`() {
		val sone = mock<Sone>().apply { whenever(id).thenReturn("sone-id") }
		val post = mock<Post>().apply {
			whenever(id).thenReturn("post-id")
			whenever(time).thenReturn(1000)
			whenever(this.sone).thenReturn(sone)
			whenever(recipientId).thenReturn("recipient-id".asOptional())
			whenever(text).thenReturn("post text")
		}
		webInterface.templateContextFactory.addAccessor(Any::class.java, ReflectionAccessor())
		addPost(post)
		addRequestParameter("post", "post-id")
		assertThatJsonIsSuccessful()
		assertThat(json["post"]!!["id"].asText(), equalTo("post-id"))
		assertThat(json["post"]!!["time"].asLong(), equalTo(1000L))
		assertThat(json["post"]!!["sone"].asText(), equalTo("sone-id"))
		assertThat(json["post"]!!["recipient"].asText(), equalTo("recipient-id"))
		assertThat(json["post"]!!["html"].asText(), equalTo(listOf(
				core.toString(),
				freenetRequest.toString(),
				"post text",
				currentSone.toString(),
				core.localSones.toString()
		).joinToString("\n")))
	}

	@Test
	fun `page can be created dependency injection`() {
	    assertThat(baseInjector.getInstance<GetPostAjaxPage>(), notNullValue())
	}

}
