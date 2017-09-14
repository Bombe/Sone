package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [LockSoneAjaxPage].
 */
class LockSoneAjaxPageTest : JsonPageTest("lockSone.ajax", requiresLogin = false, pageSupplier = ::LockSoneAjaxPage) {

	@Test
	fun `request without valid sone results in invalid-sone-id`() {
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-sone-id"))
	}

	@Test
	fun `request with valid sone id results in locked sone`() {
		val sone = mock<Sone>()
		addLocalSone("sone-id", sone)
		addRequestParameter("sone", "sone-id")
		assertThat(json.isSuccess, equalTo(true))
		verify(core).lockSone(sone)
	}

}
