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
 * Unit test for [DistrustAjaxPage].
 */
class DistrustAjaxPageTest : JsonPageTest("distrustSone.ajax", pageSupplier = ::DistrustAjaxPage) {

	@Test
	fun `request with missing sone results in invalid-sone-id`() {
		assertThatJsonFailed("invalid-sone-id")
	}

	@Test
	fun `request with invalid sone results in invalid-sone-id`() {
		addRequestParameter("sone", "invalid-sone")
		assertThatJsonFailed("invalid-sone-id")
	}

	@Test
	fun `request with valid sone results in distrusted sone`() {
		val sone = mock<Sone>()
		addSone(sone, "sone-id")
		addRequestParameter("sone", "sone-id")
		assertThatJsonIsSuccessful()
		verify(core).distrustSone(currentSone, sone)
	}

	@Test
	fun `request with valid sone results in correct trust value being sent back`() {
		core.preferences.newNegativeTrust = -33
		val sone = mock<Sone>()
		addSone(sone, "sone-id")
		addRequestParameter("sone", "sone-id")
		assertThatJsonIsSuccessful()
		assertThat(json["trustValue"]?.asInt(), equalTo(-33))
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<DistrustAjaxPage>(), notNullValue())
	}

}
