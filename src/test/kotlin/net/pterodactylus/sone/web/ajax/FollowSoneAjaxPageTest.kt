package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [FollowSoneAjaxPage].
 */
class FollowSoneAjaxPageTest : JsonPageTest("followSone.ajax", pageSupplier = ::FollowSoneAjaxPage) {

	@Test
	fun `request without sone id results in invalid-sone-id`() {
		assertThatJsonFailed("invalid-sone-id")
	}

	@Test
	fun `request with sone follows sone`() {
		addSone(mock<Sone>().apply { whenever(id).thenReturn("sone-id") })
		addRequestParameter("sone", "sone-id")
		assertThatJsonIsSuccessful()
		verify(core).followSone(currentSone, "sone-id")
	}

	@Test
	fun `request with sone makes sone as known`() {
		val sone = mock<Sone>()
		addSone(sone, "sone-id")
		addRequestParameter("sone", "sone-id")
		assertThatJsonIsSuccessful()
		verify(core).markSoneKnown(sone)
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<FollowSoneAjaxPage>(), notNullValue())
	}

}
