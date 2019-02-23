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
 * Unit test for [TrustAjaxPage].
 */
class TrustAjaxPageTest : JsonPageTest("trustSone.ajax", requiresLogin = true, needsFormPassword = true, pageSupplier = ::TrustAjaxPage) {

	private val sone = mock<Sone>()

	@Test
	fun `request with invalid sone results in invalid-sone-id`() {
		assertThatJsonFailed("invalid-sone-id")
	}

	@Test
	fun `request with valid sone trust sone`() {
		addSone(sone, "sone-id")
		addRequestParameter("sone", "sone-id")
		assertThatJsonIsSuccessful()
		verify(core).trustSone(currentSone, sone)
	}

	@Test
	fun `request with valid sone returns positive trust value`() {
		addSone(sone, "sone-id")
		addRequestParameter("sone", "sone-id")
		core.preferences.newPositiveTrust = 31
		assertThatJsonIsSuccessful()
		assertThat(json["trustValue"]?.asInt(), equalTo(31))
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<TrustAjaxPage>(), notNullValue())
	}

}
