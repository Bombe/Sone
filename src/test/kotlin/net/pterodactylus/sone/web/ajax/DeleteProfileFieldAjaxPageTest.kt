package net.pterodactylus.sone.web.ajax

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [DeleteProfileFieldAjaxPage].
 */
class DeleteProfileFieldAjaxPageTest : JsonPageTest("deleteProfileField.ajax", pageSupplier = ::DeleteProfileFieldAjaxPage) {

	@Test
	fun `request without field id results in invalid field id error`() {
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-field-id"))
	}

	@Test
	fun `request with valid field id results in field deletion`() {
		profile.addField("foo")
		val fieldId = profile.getFieldByName("foo")!!.id
		addRequestParameter("field", fieldId)
		assertThat(json.isSuccess, equalTo(true))
		assertThat(profile.getFieldByName("foo"), nullValue())
		verify(currentSone).profile = profile
		verify(core).touchConfiguration()
	}

}
