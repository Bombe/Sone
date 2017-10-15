package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [UnfollowSoneAjaxPage].
 */
class UnfollowSoneAjaxPageTest : JsonPageTest("unfollowSone.ajax", pageSupplier = ::UnfollowSoneAjaxPage) {

	@Test
	fun `request without sone returns invalid-sone-id`() {
		assertThatJsonFailed("invalid-sone-id")
	}

	@Test
	fun `request with invalid sone returns invalid-sone-id`() {
		addRequestParameter("sone", "invalid")
		assertThatJsonFailed("invalid-sone-id")
	}

	@Test
	fun `request with valid sone unfollows sone`() {
		addSone(mock<Sone>().apply { whenever(id).thenReturn("sone-id") })
		addRequestParameter("sone", "sone-id")
		assertThatJsonIsSuccessful()
		verify(core).unfollowSone(currentSone, "sone-id")
	}

}
