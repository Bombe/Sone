package net.pterodactylus.sone.web.ajax

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
