package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.web.baseInjector
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [EditProfileFieldAjaxPage].
 */
class EditProfileFieldAjaxPageTest : JsonPageTest("editProfileField.ajax", pageSupplier = ::EditProfileFieldAjaxPage) {

	@Test
	fun `request without field id results in invalid-field-id`() {
		assertThatJsonFailed("invalid-field-id")
	}

	@Test
	fun `request with empty new name results in invalid-parameter-name`() {
		val field = currentSone.profile.addField("test-field")
		addRequestParameter("field", field.id)
		addRequestParameter("name", "  \t ")
		assertThatJsonFailed("invalid-parameter-name")
	}

	@Test
	fun `request with duplicate new name results in duplicate-field-name`() {
		currentSone.profile.addField("other-field")
		val field = currentSone.profile.addField("test-field")
		addRequestParameter("field", field.id)
		addRequestParameter("name", "other-field")
		assertThatJsonFailed("duplicate-field-name")
	}

	@Test
	fun `request with valid field name changes field name`() {
		val profile = currentSone.profile
		val field = profile.addField("test-field")
		addRequestParameter("field", field.id)
		addRequestParameter("name", "  new name ")
		assertThatJsonIsSuccessful()
		assertThat(field.name, equalTo("new name"))
		verify(currentSone).profile = profile
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<EditProfileFieldAjaxPage>(), notNullValue())
	}

}
