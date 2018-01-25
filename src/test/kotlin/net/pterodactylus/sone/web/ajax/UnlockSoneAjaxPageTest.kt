package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.web.baseInjector
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.mockito.Mockito.verify

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
