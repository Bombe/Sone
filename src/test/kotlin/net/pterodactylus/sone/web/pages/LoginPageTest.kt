package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.freenet.wot.Identity
import net.pterodactylus.sone.freenet.wot.OwnIdentity
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.thenReturnMock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.pages.WebPageTest
import net.pterodactylus.sone.web.pages.LoginPage
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [LoginPage].
 */
class LoginPageTest : WebPageTest() {

	private val page = LoginPage(template, webInterface)

	private val sones = listOf(createSone("Sone", "Test"), createSone("Test"), createSone("Sone"))

	override fun getPage() = page

	private fun createSone(vararg contexts: String) = mock<Sone>().apply {
		whenever(id).thenReturn(hashCode().toString())
		val identity = mock<OwnIdentity>().apply {
			whenever(this.contexts).thenReturn(contexts.toSet())
			contexts.forEach { whenever(hasContext(it)).thenReturn(true) }
		}
		whenever(this.identity).thenReturn(identity)
		whenever(profile).thenReturnMock()
	}

	@Before
	fun setupSones() {
		addLocalSone("sone1", sones[0])
		addLocalSone("sone2", sones[1])
		addLocalSone("sone3", sones[2])
		addOwnIdentity(sones[0].identity as OwnIdentity)
		addOwnIdentity(sones[1].identity as OwnIdentity)
		addOwnIdentity(sones[2].identity as OwnIdentity)
	}

	@Test
	fun `page returns correct path`() {
	    assertThat(page.path, equalTo("login.html"))
	}

	@Test
	fun `page does not require login`() {
	    assertThat(page.requiresLogin(), equalTo(false))
	}

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `get request stores sones in template context`() {
		request("", GET)
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["sones"] as Iterable<Sone>, containsInAnyOrder(sones[0], sones[1], sones[2]))
	}

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `get request stores identities without sones in template context`() {
		request("", GET)
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["identitiesWithoutSone"] as Iterable<Identity>, contains(sones[1].identity))
	}

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `post request with invalid sone sets sones and identities without sone in template context`() {
		request("", POST)
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["sones"] as Iterable<Sone>, containsInAnyOrder(sones[0], sones[1], sones[2]))
		assertThat(templateContext["identitiesWithoutSone"] as Iterable<Identity>, contains(sones[1].identity))
	}

	@Test
	fun `post request with valid sone logs in the sone and redirects to index page`() {
		request("", POST)
		addHttpRequestParameter("sone-id", "sone2")
		verifyRedirect("index.html") {
			verify(webInterface).setCurrentSone(toadletContext, sones[1])
		}
	}

	@Test
	fun `post request with valid sone and target redirects to target page`() {
		request("", POST)
		addHttpRequestParameter("sone-id", "sone2")
		addHttpRequestParameter("target", "foo.html")
		verifyRedirect("foo.html") {
			verify(webInterface).setCurrentSone(toadletContext, sones[1])
		}
	}

	@Test
	fun `redirect to index html if a sone is logged in`() {
		assertThat(page.getRedirectTarget(freenetRequest), equalTo("index.html"))
	}

	@Test
	fun `do not redirect if no sone is logged in`() {
		unsetCurrentSone()
		assertThat(page.getRedirectTarget(freenetRequest), nullValue())
	}

	@Test
	fun `page is not enabled if full access required and request is not full access`() {
		core.preferences.isRequireFullAccess = true
		assertThat(page.isEnabled(toadletContext), equalTo(false))
	}

	@Test
	fun `page is enabled if no full access is required and there is no current sone`() {
		unsetCurrentSone()
		assertThat(page.isEnabled(toadletContext), equalTo(true))
	}

	@Test
	fun `page is not enabled if no full access is required but there is a current sone`() {
		assertThat(page.isEnabled(toadletContext), equalTo(false))
	}

	@Test
	fun `page is enabled if full access required and request is full access and there is no current sone`() {
		core.preferences.isRequireFullAccess = true
		unsetCurrentSone()
		whenever(toadletContext.isAllowedFullAccess).thenReturn(true)
		assertThat(page.isEnabled(toadletContext), equalTo(true))
	}

	@Test
	fun `page is not enabled if full access required and request is full access but there is a current sone`() {
		core.preferences.isRequireFullAccess = true
		whenever(toadletContext.isAllowedFullAccess).thenReturn(true)
		assertThat(page.isEnabled(toadletContext), equalTo(false))
	}

}
