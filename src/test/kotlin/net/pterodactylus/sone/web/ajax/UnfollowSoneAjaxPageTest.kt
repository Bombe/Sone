package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

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

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<UnfollowSoneAjaxPage>(), notNullValue())
	}

}
