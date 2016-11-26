package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.WebTestUtils.redirectsTo
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [EditProfileFieldPage].
 */
class EditProfileFieldPageTest : WebPageTest() {

	private val page = EditProfileFieldPage(template, webInterface)

	private val profile = Profile(currentSone)
	private val field = profile.addField("Name")

	@Before
	fun setupProfile() {
		whenever(currentSone.profile).thenReturn(profile)
	}

	@Test
	fun `get request with invalid field redirects to invalid page`() {
		request("", GET)
		expectedException.expect(redirectsTo("invalid.html"))
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `get request with valid field stores field in template context`() {
		request("", GET)
		addHttpRequestParameter("field", field.id)
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["field"], equalTo<Any>(field))
	}

	@Test
	fun `post request with cancel set redirects to profile edit page`() {
		request("", POST)
		addHttpRequestParameter("field", field.id)
		addHttpRequestParameter("cancel", "true")
		expectedException.expect(redirectsTo("editProfile.html#profile-fields"))
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request with new name renames field and redirects to profile edit page`() {
		request("", POST)
		addHttpRequestParameter("field", field.id)
		addHttpRequestParameter("name", "New Name")
		expectedException.expect(redirectsTo("editProfile.html#profile-fields"))
		try {
			page.handleRequest(freenetRequest, templateContext)
		} finally {
			assertThat(field.name, equalTo("New Name"))
			verify(currentSone).profile = profile
		}
	}

	@Test
	fun `post request with same name does not modify field and redirects to profile edit page`() {
		request("", POST)
		addHttpRequestParameter("field", field.id)
		addHttpRequestParameter("name", "Name")
		expectedException.expect(redirectsTo("editProfile.html#profile-fields"))
		try {
			page.handleRequest(freenetRequest, templateContext)
		} finally {
			assertThat(field.name, equalTo("Name"))
			verify(currentSone, never()).profile = profile
		}
	}

	@Test
	fun `post request with same name as different field sets error condition in template`() {
		request("", POST)
		profile.addField("New Name")
		addHttpRequestParameter("field", field.id)
		addHttpRequestParameter("name", "New Name")
		page.handleRequest(freenetRequest, templateContext)
		assertThat(field.name, equalTo("Name"))
		verify(currentSone, never()).profile = profile
		assertThat(templateContext["duplicateFieldName"], equalTo<Any>(true))
	}

}
