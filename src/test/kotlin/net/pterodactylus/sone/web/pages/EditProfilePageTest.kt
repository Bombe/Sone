package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Image
import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [EditProfilePage].
 */
class EditProfilePageTest: WebPageTest(::EditProfilePage) {

	private val profile = Profile(currentSone)
	private val firstField = profile.addField("First Field")
	private val secondField = profile.addField("Second Field")

	@Before
	fun setupProfile() {
		val avatar = mock<Image>()
		whenever(avatar.id).thenReturn("image-id")
		whenever(avatar.sone).thenReturn(currentSone)
		profile.firstName = "First"
		profile.middleName = "Middle"
		profile.lastName = "Last"
		profile.birthDay = 31
		profile.birthMonth = 12
		profile.birthYear = 1999
		profile.setAvatar(avatar)
		whenever(currentSone.profile).thenReturn(profile)
	}

	@Test
	fun `page returns correct path`() {
	    assertThat(page.path, equalTo("editProfile.html"))
	}

	@Test
	fun `page requires login`() {
	    assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `page returns correct title`() {
	    whenever(l10n.getString("Page.EditProfile.Title")).thenReturn("edit profile page title")
		assertThat(page.getPageTitle(freenetRequest), equalTo("edit profile page title"))
	}

	@Test
	fun `get request stores fields of current sone’s profile in template context`() {
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["firstName"], equalTo<Any>("First"))
		assertThat(templateContext["middleName"], equalTo<Any>("Middle"))
		assertThat(templateContext["lastName"], equalTo<Any>("Last"))
		assertThat(templateContext["birthDay"], equalTo<Any>(31))
		assertThat(templateContext["birthMonth"], equalTo<Any>(12))
		assertThat(templateContext["birthYear"], equalTo<Any>(1999))
		assertThat(templateContext["avatarId"], equalTo<Any>("image-id"))
		assertThat(templateContext["fields"], equalTo<Any>(listOf(firstField, secondField)))
	}

	@Test
	fun `post request without any command stores fields of current sone’s profile in template context`() {
		setMethod(POST)
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["firstName"], equalTo<Any>("First"))
		assertThat(templateContext["middleName"], equalTo<Any>("Middle"))
		assertThat(templateContext["lastName"], equalTo<Any>("Last"))
		assertThat(templateContext["birthDay"], equalTo<Any>(31))
		assertThat(templateContext["birthMonth"], equalTo<Any>(12))
		assertThat(templateContext["birthYear"], equalTo<Any>(1999))
		assertThat(templateContext["avatarId"], equalTo<Any>("image-id"))
		assertThat(templateContext["fields"], equalTo<Any>(listOf(firstField, secondField)))
	}

	private fun <T> verifySingleFieldCanBeChanged(fieldName: String, newValue: T, expectedValue: T = newValue, fieldAccessor: () -> T) {
		setMethod(POST)
		addHttpRequestPart("save-profile", "true")
		addHttpRequestPart(fieldName, newValue.toString())
		verifyRedirect("editProfile.html") {
			verify(currentSone).profile = profile
			verify(core).touchConfiguration()
			assertThat(fieldAccessor(), equalTo(expectedValue))
		}
	}

	@Test
	fun `post request with new first name and save profile saves the profile and redirects back to profile edit page`() {
		verifySingleFieldCanBeChanged("first-name", "New First") { profile.firstName }
	}

	@Test
	fun `post request with new middle name and save profile saves the profile and redirects back to profile edit page`() {
		verifySingleFieldCanBeChanged("middle-name", "New Middle") { profile.middleName }
	}

	@Test
	fun `post request with new last name and save profile saves the profile and redirects back to profile edit page`() {
		verifySingleFieldCanBeChanged("last-name", "New Last") { profile.lastName }
	}

	@Test
	fun `post request with new birth day and save profile saves the profile and redirects back to profile edit page`() {
		verifySingleFieldCanBeChanged("birth-day", 1) { profile.birthDay }
	}

	@Test
	fun `post request with new birth month and save profile saves the profile and redirects back to profile edit page`() {
		verifySingleFieldCanBeChanged("birth-month", 1) { profile.birthMonth }
	}

	@Test
	fun `post request with new birth year and save profile saves the profile and redirects back to profile edit page`() {
		verifySingleFieldCanBeChanged("birth-year", 1) { profile.birthYear }
	}

	@Test
	fun `post request with new avatar ID and save profile saves the profile and redirects back to profile edit page`() {
		val newAvatar = mock<Image>()
		whenever(newAvatar.sone).thenReturn(currentSone)
		whenever(newAvatar.id).thenReturn("avatar-id")
		addImage("avatar-id", newAvatar)
		verifySingleFieldCanBeChanged("avatarId", "avatar-id") { profile.avatar }
	}

	@Test
	fun `post request with field value saves profile and redirects back to profile edit page`() {
		val field = profile.addField("name")
		field.value = "old"
		verifySingleFieldCanBeChanged("field-${field.id}", "new") { profile.getFieldByName("name")!!.value }
	}

	@Test
	fun `post request with field value saves filtered value to profile and redirects back to profile edit page`() {
		val field = profile.addField("name")
		field.value = "old"
		addHttpRequestHeader("Host", "www.te.st")
		verifySingleFieldCanBeChanged("field-${field.id}", "http://www.te.st/KSK@GPL.txt", "KSK@GPL.txt") { profile.getFieldByName("name")!!.value }
	}

	@Test
	fun `adding a field with a duplicate name sets error in template context`() {
		setMethod(POST)
		profile.addField("new-field")
		addHttpRequestPart("add-field", "true")
		addHttpRequestPart("field-name", "new-field")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["fieldName"], equalTo<Any>("new-field"))
		assertThat(templateContext["duplicateFieldName"], equalTo<Any>(true))
		verify(core, never()).touchConfiguration()
	}

	@Test
	fun `adding a field with a new name sets adds field to profile and redirects to profile edit page`() {
		setMethod(POST)
		addHttpRequestPart("add-field", "true")
		addHttpRequestPart("field-name", "new-field")
		verifyRedirect("editProfile.html#profile-fields") {
			assertThat(profile.getFieldByName("new-field"), notNullValue())
			verify(currentSone).profile = profile
			verify(core).touchConfiguration()
		}
	}

	@Test
	fun `deleting a field redirects to delete field page`() {
		setMethod(POST)
		addHttpRequestPart("delete-field-${firstField.id}", "true")
		verifyRedirect("deleteProfileField.html?field=${firstField.id}")
	}

	@Test
	fun `moving a field up moves the field up and redirects to the edit profile page`() {
		setMethod(POST)
		addHttpRequestPart("move-up-field-${secondField.id}", "true")
		verifyRedirect("editProfile.html#profile-fields") {
			assertThat(profile.fields, contains(secondField, firstField))
			verify(currentSone).profile = profile
		}
	}

	@Test
	fun `moving an invalid field up does not redirect`() {
		setMethod(POST)
		addHttpRequestPart("move-up-field-foo", "true")
		page.processTemplate(freenetRequest, templateContext)
	}

	@Test
	fun `moving a field down moves the field down and redirects to the edit profile page`() {
		setMethod(POST)
		addHttpRequestPart("move-down-field-${firstField.id}", "true")
		verifyRedirect("editProfile.html#profile-fields") {
			assertThat(profile.fields, contains(secondField, firstField))
			verify(currentSone).profile = profile
		}
	}

	@Test
	fun `moving an invalid field down does not redirect`() {
		setMethod(POST)
		addHttpRequestPart("move-down-field-foo", "true")
		page.processTemplate(freenetRequest, templateContext)
	}

	@Test
	fun `editing a field redirects to the edit profile page`() {
		setMethod(POST)
		addHttpRequestPart("edit-field-${firstField.id}", "true")
		verifyRedirect("editProfileField.html?field=${firstField.id}")
	}

}
