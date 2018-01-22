package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.web.baseInjector
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [DeleteProfileFieldAjaxPage].
 */
class DeleteProfileFieldAjaxPageTest : JsonPageTest("deleteProfileField.ajax", pageSupplier = ::DeleteProfileFieldAjaxPage) {

	@Test
	fun `request without field id results in invalid field id error`() {
		assertThatJsonFailed("invalid-field-id")
	}

	@Test
	fun `request with valid field id results in field deletion`() {
		profile.addField("foo")
		val fieldId = profile.getFieldByName("foo")!!.id
		addRequestParameter("field", fieldId)
		assertThatJsonIsSuccessful()
		assertThat(profile.getFieldByName("foo"), nullValue())
		verify(currentSone).profile = profile
		verify(core).touchConfiguration()
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<DeleteProfileFieldAjaxPage>(), notNullValue())
	}

}
