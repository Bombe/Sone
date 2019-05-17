package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [DeleteProfileFieldPage].
 */
class DeleteProfileFieldPageTest : WebPageTest(::DeleteProfileFieldPage) {

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

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<DeleteProfileFieldPage>(), notNullValue())
	}

	@Test
	fun `page is annotated with correct template path`() {
		assertThat(page.templatePath, equalTo("/templates/deleteProfileField.html"))
	}

}
