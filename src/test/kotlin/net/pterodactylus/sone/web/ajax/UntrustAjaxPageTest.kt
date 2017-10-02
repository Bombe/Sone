package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [UntrustAjaxPage].
 */
class UntrustAjaxPageTest : JsonPageTest("untrustSone.ajax", pageSupplier = ::UntrustAjaxPage) {

	@Test
	fun `request without sone results in invalid-sone-id`() {
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-sone-id"))
	}

	@Test
	fun `request with invalid sone results in invalid-sone-id`() {
		addRequestParameter("sone", "invalid")
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-sone-id"))
	}

	@Test
	fun `request with valid sone results in sone being untrusted`() {
		val sone = mock<Sone>()
		addSone(sone, "sone-id")
		addRequestParameter("sone", "sone-id")
		assertThat(json.isSuccess, equalTo(true))
		verify(core).untrustSone(currentSone, sone)
	}

	@Test
	fun `request with valid sone results in null trust value being returned`() {
		val sone = mock<Sone>()
		addSone(sone, "sone-id")
		addRequestParameter("sone", "sone-id")
		assertThat(json.isSuccess, equalTo(true))
		assertThat(json["trustValue"], nullValue())
	}

}
