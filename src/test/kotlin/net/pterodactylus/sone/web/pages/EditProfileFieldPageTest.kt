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
 * Unit test for [EditProfileFieldPage].
 */
class EditProfileFieldPageTest : WebPageTest(::EditProfileFieldPage) {

	private val profile = Profile(currentSone)
	private val field = profile.addField("Name")

	@Before
	fun setupProfile() {
		whenever(currentSone.profile).thenReturn(profile)
	}

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("editProfileField.html"))
	}

	@Test
	fun `page requires login`() {
		assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `page returns correct title`() {
		addTranslation("Page.EditProfileField.Title", "edit profile field title")
		assertThat(page.getPageTitle(soneRequest), equalTo("edit profile field title"))
	}

	@Test
	fun `get request with invalid field redirects to invalid page`() {
		verifyRedirect("invalid.html")
	}

	@Test
	fun `get request with valid field stores field in template context`() {
		addHttpRequestParameter("field", field.id)
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["field"], equalTo<Any>(field))
	}

	@Test
	fun `post request with cancel set redirects to profile edit page`() {
		setMethod(POST)
		addHttpRequestPart("field", field.id)
		addHttpRequestPart("cancel", "true")
		verifyRedirect("editProfile.html#profile-fields")
	}

	@Test
	fun `post request with new name renames field and redirects to profile edit page`() {
		setMethod(POST)
		addHttpRequestPart("field", field.id)
		addHttpRequestPart("name", "New Name")
		verifyRedirect("editProfile.html#profile-fields") {
			assertThat(field.name, equalTo("New Name"))
			verify(currentSone).profile = profile
		}
	}

	@Test
	fun `post request with same name does not modify field and redirects to profile edit page`() {
		setMethod(POST)
		addHttpRequestPart("field", field.id)
		addHttpRequestPart("name", "Name")
		verifyRedirect("editProfile.html#profile-fields") {
			assertThat(field.name, equalTo("Name"))
			verify(currentSone, never()).profile = profile
		}
	}

	@Test
	fun `post request with same name as different field sets error condition in template`() {
		setMethod(POST)
		profile.addField("New Name")
		addHttpRequestPart("field", field.id)
		addHttpRequestPart("name", "New Name")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(field.name, equalTo("Name"))
		verify(currentSone, never()).profile = profile
		assertThat(templateContext["duplicateFieldName"], equalTo<Any>(true))
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<EditProfileFieldPage>(), notNullValue())
	}

	@Test
	fun `page is annotated with correct template path`() {
		assertThat(page.templatePath, equalTo("/templates/editProfileField.html"))
	}

}
