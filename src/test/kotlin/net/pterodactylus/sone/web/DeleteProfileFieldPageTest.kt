package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.WebTestUtils.redirectsTo
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import kotlin.test.fail

/**
 * Unit test for [DeleteProfileFieldPage].
 */
class DeleteProfileFieldPageTest : WebPageTest() {

	private val page = DeleteProfileFieldPage(template, webInterface)

	private val profile = Profile(currentSone)
	private val field = profile.addField("name")

	@Before
	fun setupProfile() {
		whenever(currentSone.profile).thenReturn(profile)
		field.value = "value"
	}

	@Test
	fun `get request with invalid field name redirects to invalid page`() {
		request("", GET)
		expectedException.expect(redirectsTo("invalid.html"))
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request with invalid field name redirects to invalid page`() {
		request("", POST)
		addHttpRequestParameter("field", "wrong-id")
		expectedException.expect(redirectsTo("invalid.html"))
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `get request with valid field name sets field in template context`() {
		request("", GET)
		addHttpRequestParameter("field", field.id)
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["field"], equalTo<Any>(field))
	}

	@Test
	fun `post request without confirm redirects to edit profile page`() {
		request("", POST)
		addHttpRequestParameter("field", field.id)
		expectedException.expect(redirectsTo("editProfile.html#profile-fields"))
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request with confirm removes field and redirects to edit profile page`() {
		request("", POST)
		addHttpRequestParameter("field", field.id)
		addHttpRequestParameter("confirm", "true")
		expectedException.expect(redirectsTo("editProfile.html#profile-fields"))
		try {
			page.handleRequest(freenetRequest, templateContext)
			fail()
		} catch (e: Exception) {
			assertThat(profile.getFieldById(field.id), nullValue())
			verify(currentSone).profile = profile
			throw e
		}
	}

}
