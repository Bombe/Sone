package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.web.baseInjector
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [UnbookmarkAjaxPage].
 */
class UnbookmarkAjaxPageTest : JsonPageTest("unbookmark.ajax", requiresLogin = false, needsFormPassword = true, pageSupplier = ::UnbookmarkAjaxPage) {

	@Test
	fun `request without post id results in invalid-post-id`() {
		assertThatJsonFailed("invalid-post-id")
	}

	@Test
	fun `request with empty post id results in invalid-post-id`() {
		addRequestParameter("post", "")
		assertThatJsonFailed("invalid-post-id")
	}

	@Test
	fun `request with invalid post id does not unbookmark anything and fails`() {
		addRequestParameter("post", "invalid")
		assertThat(json.isSuccess, equalTo(false))
		verify(core, never()).unbookmarkPost(any())
	}

	@Test
	fun `request with valid post id does not unbookmark anything but succeeds`() {
		val post = mock<Post>()
		addPost(post, "post-id")
		addRequestParameter("post", "post-id")
		assertThatJsonIsSuccessful()
		verify(core).unbookmarkPost(eq(post))
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<UnbookmarkAjaxPage>(), notNullValue())
	}

}
