package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.baseInjector
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.mockito.Mockito.verify

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
