package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [DistrustAjaxPage].
 */
class DistrustAjaxPageTest : JsonPageTest("distrustSone.ajax", pageSupplier = ::DistrustAjaxPage) {

	@Test
	fun `request with missing sone results in invalid-sone-id`() {
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-sone-id"))
	}

	@Test
	fun `request with invalid sone results in invalid-sone-id`() {
		addRequestParameter("sone", "invalid-sone")
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-sone-id"))
	}

	@Test
	fun `request with valid sone results in distrusted sone`() {
		val sone = mock<Sone>()
		addSone(sone, "sone-id")
		addRequestParameter("sone", "sone-id")
		assertThat(json.isSuccess, equalTo(true))
		verify(core).distrustSone(currentSone, sone)
	}

	@Test
	fun `request with valid sone results in correct trust value being sent back`() {
		core.preferences.negativeTrust = -33
		val sone = mock<Sone>()
		addSone(sone, "sone-id")
		addRequestParameter("sone", "sone-id")
		assertThat(json.isSuccess, equalTo(true))
		assertThat(json["trustValue"]?.asInt(), equalTo(-33))
	}

}
