package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [DeletePostAjaxPage].
 */
class DeletePostAjaxPageTest : JsonPageTest("deletePost.ajax", pageSupplier = ::DeletePostAjaxPage) {

	@Test
	fun `missing post ID results in invalid id response`() {
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-post-id"))
	}

	@Test
	fun `post from non-local sone results in not authorized response`() {
		val post = mock<Post>()
		val sone = mock<Sone>()
		whenever(post.sone).thenReturn(sone)
		addPost("post-id", post)
		addRequestParameter("post", "post-id")
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("not-authorized"))
	}

	@Test
	fun `post from local sone is deleted`() {
		val post = mock<Post>()
		val sone = mock<Sone>().apply { whenever(isLocal).thenReturn(true) }
		whenever(post.sone).thenReturn(sone)
		addPost("post-id", post)
		addRequestParameter("post", "post-id")
		assertThat(json.isSuccess, equalTo(true))
		verify(core).deletePost(post)
	}

}
