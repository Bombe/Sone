package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.freenet.wot.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [LoginPage].
 */
class LoginPageTest : WebPageTest(::LoginPage) {

	private val sones = listOf(createSone("Sone", "Test"), createSone("Test"), createSone("Sone"))

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
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["sones"] as Iterable<Sone>, containsInAnyOrder(sones[0], sones[1], sones[2]))
	}

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `get request stores identities without sones in template context`() {
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["identitiesWithoutSone"] as Iterable<Identity>, contains(sones[1].identity))
	}

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `post request with invalid sone sets sones and identities without sone in template context`() {
		setMethod(POST)
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["sones"] as Iterable<Sone>, containsInAnyOrder(sones[0], sones[1], sones[2]))
		assertThat(templateContext["identitiesWithoutSone"] as Iterable<Identity>, contains(sones[1].identity))
	}

	@Test
	fun `post request with valid sone logs in the sone and redirects to index page`() {
		setMethod(POST)
		addHttpRequestPart("sone-id", "sone2")
		verifyRedirect("index.html") {
			verify(webInterface).setCurrentSone(toadletContext, sones[1])
		}
	}

	@Test
	fun `post request with valid sone and target redirects to target page`() {
		setMethod(POST)
		addHttpRequestPart("sone-id", "sone2")
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
		core.preferences.newRequireFullAccess = true
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
		core.preferences.newRequireFullAccess = true
		unsetCurrentSone()
		whenever(toadletContext.isAllowedFullAccess).thenReturn(true)
		assertThat(page.isEnabled(toadletContext), equalTo(true))
	}

	@Test
	fun `page is not enabled if full access required and request is full access but there is a current sone`() {
		core.preferences.newRequireFullAccess = true
		whenever(toadletContext.isAllowedFullAccess).thenReturn(true)
		assertThat(page.isEnabled(toadletContext), equalTo(false))
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<LoginPage>(), notNullValue())
	}

	@Test
	fun `page is annotated with correct menuname`() {
		assertThat(page.menuName, equalTo("Login"))
	}

	@Test
	fun `page is annotated with correct template path`() {
		assertThat(page.templatePath, equalTo("/templates/login.html"))
	}

}
