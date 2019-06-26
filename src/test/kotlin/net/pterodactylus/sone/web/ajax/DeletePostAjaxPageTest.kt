package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [DeletePostAjaxPage].
 */
class DeletePostAjaxPageTest : JsonPageTest("deletePost.ajax", pageSupplier = ::DeletePostAjaxPage) {

	@Test
	fun `missing post ID results in invalid id response`() {
		assertThatJsonFailed("invalid-post-id")
	}

	@Test
	fun `post from non-local sone results in not authorized response`() {
		val post = mock<Post>()
		val sone = mock<Sone>()
		whenever(post.sone).thenReturn(sone)
		addPost(post, "post-id")
		addRequestParameter("post", "post-id")
		assertThatJsonFailed("not-authorized")
	}

	@Test
	fun `post from local sone is deleted`() {
		val post = mock<Post>()
		val sone = mock<Sone>().apply { whenever(isLocal).thenReturn(true) }
		whenever(post.sone).thenReturn(sone)
		addPost(post, "post-id")
		addRequestParameter("post", "post-id")
		assertThatJsonIsSuccessful()
		verify(core).deletePost(post)
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<DeletePostAjaxPage>(), notNullValue())
	}

}
