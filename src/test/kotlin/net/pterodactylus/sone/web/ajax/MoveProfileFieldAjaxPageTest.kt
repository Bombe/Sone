package net.pterodactylus.sone.web.ajax

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [MoveProfileFieldAjaxPage].
 */
class MoveProfileFieldAjaxPageTest : JsonPageTest("moveProfileField.ajax", true, true, ::MoveProfileFieldAjaxPage) {

	@Test
	fun `request without field id results in invalid-field-id`() {
		assertThatJsonFailed("invalid-field-id")
	}

	@Test
	fun `request with invalid direction results in invalid-direction`() {
		val fieldId = profile.addField("someField").id
		addRequestParameter("field", fieldId)
		assertThatJsonFailed("invalid-direction")
	}

	@Test
	fun `moving first field up results in not-possible`() {
		val fieldId = profile.addField("someField").id
		addRequestParameter("field", fieldId)
		addRequestParameter("direction", "up")
		assertThatJsonFailed("not-possible")
	}

	@Test
	fun `moving only field down results in not-possible`() {
		val fieldId = profile.addField("someField").id
		addRequestParameter("field", fieldId)
		addRequestParameter("direction", "down")
		assertThatJsonFailed("not-possible")
	}

	@Test
	fun `moving second field up results in field being moved up`() {
		profile.addField("firstField")
		val fieldId = profile.addField("someField").id
		addRequestParameter("field", fieldId)
		addRequestParameter("direction", "up")
		assertThatJsonIsSuccessful()
		assertThat(profile.fields[0].id, equalTo(fieldId))
		verify(core).touchConfiguration()
		verify(currentSone).profile = profile
	}

	@Test
	fun `moving first field down results in field being moved down`() {
		val fieldId = profile.addField("someField").id
		profile.addField("firstField")
		addRequestParameter("field", fieldId)
		addRequestParameter("direction", "down")
		assertThatJsonIsSuccessful()
		assertThat(profile.fields.last().id, equalTo(fieldId))
		verify(core).touchConfiguration()
		verify(currentSone).profile = profile
	}

}
