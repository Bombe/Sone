package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [UnlockSoneAjaxPage].
 */
class UnlockSoneAjaxPageTest : JsonPageTest("unlockSone.ajax", requiresLogin = false, pageSupplier = ::UnlockSoneAjaxPage) {

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
	fun `request with valid sone results in locked sone`() {
		val sone = mock<Sone>()
		addLocalSone(sone, "sone-id")
		addRequestParameter("sone", "sone-id")
		assertThatJsonIsSuccessful()
		verify(core).unlockSone(sone)
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<UnlockSoneAjaxPage>(), notNullValue())
	}

}
