package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [UntrustAjaxPage].
 */
class UntrustAjaxPageTest : JsonPageTest("untrustSone.ajax", pageSupplier = ::UntrustAjaxPage) {

	@Test
	fun `request without sone results in invalid-sone-id`() {
		assertThatJsonFailed("invalid-sone-id")
	}

	@Test
	fun `request with invalid sone results in invalid-sone-id`() {
		addRequestParameter("sone", "invalid")
		assertThatJsonFailed("invalid-sone-id")
	}

	@Test
	fun `request with valid sone results in sone being untrusted`() {
		val sone = mock<Sone>()
		addSone(sone, "sone-id")
		addRequestParameter("sone", "sone-id")
		assertThatJsonIsSuccessful()
		verify(core).untrustSone(currentSone, sone)
	}

	@Test
	fun `request with valid sone results in null trust value being returned`() {
		val sone = mock<Sone>()
		addSone(sone, "sone-id")
		addRequestParameter("sone", "sone-id")
		assertThatJsonIsSuccessful()
		assertThat(json["trustValue"], nullValue())
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<UntrustAjaxPage>(), notNullValue())
	}

}
