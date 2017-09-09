package net.pterodactylus.sone.web.ajax

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [EditProfileFieldAjaxPage].
 */
class EditProfileFieldAjaxPageTest : JsonPageTest("editProfileField.ajax", pageSupplier = ::EditProfileFieldAjaxPage) {

	@Test
	fun `request without field id results in invalid-field-id`() {
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-field-id"))
	}

	@Test
	fun `request with empty new name results in invalid-parameter-name`() {
		val field = currentSone.profile.addField("test-field")
		addRequestParameter("field", field.id)
		addRequestParameter("name", "  \t ")
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-parameter-name"))
	}

	@Test
	fun `request with duplicate new name results in duplicate-field-name`() {
		currentSone.profile.addField("other-field")
		val field = currentSone.profile.addField("test-field")
		addRequestParameter("field", field.id)
		addRequestParameter("name", "other-field")
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("duplicate-field-name"))
	}

	@Test
	fun `request with valid field name changes field name`() {
		val profile = currentSone.profile
		val field = profile.addField("test-field")
		addRequestParameter("field", field.id)
		addRequestParameter("name", "  new name ")
		assertThat(json.isSuccess, equalTo(true))
		assertThat(field.name, equalTo("new name"))
		verify(currentSone).profile = profile
	}

}
