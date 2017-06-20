package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [DeleteProfileFieldPage].
 */
class DeleteProfileFieldPageTest: WebPageTest2(::DeleteProfileFieldPage) {

	private val profile = Profile(currentSone)
	private val field = profile.addField("name")

	@Before
	fun setupProfile() {
		whenever(currentSone.profile).thenReturn(profile)
		field.value = "value"
	}

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("deleteProfileField.html"))
	}

	@Test
	fun `page requires login`() {
		assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `get request with invalid field name redirects to invalid page`() {
		verifyRedirect("invalid.html")
	}

	@Test
	fun `post request with invalid field name redirects to invalid page`() {
		setMethod(POST)
		addHttpRequestPart("field", "wrong-id")
		verifyRedirect("invalid.html")
	}

	@Test
	fun `get request with valid field name sets field in template context`() {
		addHttpRequestParameter("field", field.id)
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["field"], equalTo<Any>(field))
	}

	@Test
	fun `post request without confirm redirects to edit profile page`() {
		setMethod(POST)
		addHttpRequestPart("field", field.id)
		verifyRedirect("editProfile.html#profile-fields") {
			verify(currentSone, never()).profile = any()
		}
	}

	@Test
	fun `post request with confirm removes field and redirects to edit profile page`() {
		setMethod(POST)
		addHttpRequestPart("field", field.id)
		addHttpRequestPart("confirm", "true")
		verifyRedirect("editProfile.html#profile-fields") {
			assertThat(profile.getFieldById(field.id), nullValue())
			verify(currentSone).profile = profile
		}
	}

}
