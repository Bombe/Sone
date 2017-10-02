package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [TrustAjaxPage].
 */
class TrustAjaxPageTest : JsonPageTest("trustSone.ajax", requiresLogin = true, needsFormPassword = true, pageSupplier = ::TrustAjaxPage) {

	private val sone = mock<Sone>()

	@Test
	fun `request with invalid sone results in invalid-sone-id`() {
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-sone-id"))
	}

	@Test
	fun `request with valid sone trust sone`() {
		addSone(sone, "sone-id")
		addRequestParameter("sone", "sone-id")
		assertThat(json.isSuccess, equalTo(true))
		verify(core).trustSone(currentSone, sone)
	}

	@Test
	fun `request with valid sone returns positive trust value`() {
		addSone(sone, "sone-id")
		addRequestParameter("sone", "sone-id")
		core.preferences.positiveTrust = 31
		assertThat(json.isSuccess, equalTo(true))
		assertThat(json["trustValue"].asInt(), equalTo(31))
	}

}
