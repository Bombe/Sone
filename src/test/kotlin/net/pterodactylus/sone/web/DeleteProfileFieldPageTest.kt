package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [DeleteProfileFieldPage].
 */
class DeleteProfileFieldPageTest : WebPageTest() {

	private val page = DeleteProfileFieldPage(template, webInterface)

	private val profile = Profile(currentSone)
	private val field = profile.addField("name")

	override fun getPage() = page

	@Before
	fun setupProfile() {
		whenever(currentSone.profile).thenReturn(profile)
		field.value = "value"
	}

	@Test
	fun `get request with invalid field name redirects to invalid page`() {
		request("", GET)
		verifyRedirect("invalid.html")
	}

	@Test
	fun `post request with invalid field name redirects to invalid page`() {
		request("", POST)
		addHttpRequestParameter("field", "wrong-id")
		verifyRedirect("invalid.html")
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
		verifyRedirect("editProfile.html#profile-fields")
	}

	@Test
	fun `post request with confirm removes field and redirects to edit profile page`() {
		request("", POST)
		addHttpRequestParameter("field", field.id)
		addHttpRequestParameter("confirm", "true")
		verifyRedirect("editProfile.html#profile-fields") {
			assertThat(profile.getFieldById(field.id), nullValue())
			verify(currentSone).profile = profile
		}
	}

}
