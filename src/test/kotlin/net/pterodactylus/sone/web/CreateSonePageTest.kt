package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.freenet.wot.OwnIdentity
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.verify

/**
 * Unit test for [CreateSonePage].
 */
class CreateSonePageTest: WebPageTest() {

	private val page = CreateSonePage(template, webInterface)
	override fun getPage() = page

	private val localSones_ = listOf(
			createSone("local-sone1"),
			createSone("local-sone2"),
			createSone("local-sone3")
	)

	private fun createSone(id: String) = mock<Sone>().apply {
		whenever(this.id).thenReturn(id)
		whenever(profile).thenReturn(Profile(this))
	}

	private val ownIdentities_ = listOf(
			createOwnIdentity("own-id-1", "Sone"),
			createOwnIdentity("own-id-2", "Test", "Foo"),
			createOwnIdentity("own-id-3"),
			createOwnIdentity("own-id-4", "Sone")
	)

	private fun createOwnIdentity(id: String, vararg contexts: String) = mock<OwnIdentity>().apply {
		whenever(this.id).thenReturn(id)
		whenever(this.nickname).thenReturn(id)
		whenever(this.contexts).thenReturn(contexts.toSet())
		whenever(this.hasContext(anyString())).thenAnswer { invocation -> invocation.getArgument<String>(0) in contexts }
	}

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("createSone.html"))
	}

	@Test
	fun `page does not require login`() {
		assertThat(page.requiresLogin(), equalTo(false))
	}

	private fun addExistingSones() {
		listOf(2, 0, 1).map { localSones_[it] }.forEach { addLocalSone(it.id, it) }
	}

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `get request stores sorted list of local sones in template context`() {
		addExistingSones()
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["sones"] as Collection<Sone>, contains(localSones_[0], localSones_[1], localSones_[2]))
	}

	private fun addExistingOwnIdentities() {
		listOf(2, 0, 3, 1).map { ownIdentities_[it] }.forEach { addOwnIdentity(it) }
	}

	@Test
	@Suppress("UNCHECKED_CAST")
	fun `get request stores sorted sones without sone context in the template context`() {
		addExistingOwnIdentities()
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["identitiesWithoutSone"] as Collection<OwnIdentity>, contains(ownIdentities_[1], ownIdentities_[2]))
	}

	@Test
	fun `sone is created and logged in`() {
		addExistingOwnIdentities()
		request("", POST)
		addHttpRequestParameter("identity", "own-id-3")
		val newSone = mock<Sone>()
		whenever(core.createSone(ownIdentities_[2])).thenReturn(newSone)
		verifyRedirect("index.html") {
			verify(webInterface).setCurrentSone(toadletContext, newSone)
		}
	}

	@Test
	fun `on invalid identity id a flag is set in the template context`() {
		request("", POST)
		addHttpRequestParameter("identity", "own-id-3")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["errorNoIdentity"], equalTo<Any>(true))
	}

	@Test
	fun `if sone is not created user is still redirected to index`() {
		addExistingOwnIdentities()
		request("", POST)
		addHttpRequestParameter("identity", "own-id-3")
		whenever(core.createSone(ownIdentities_[2])).thenReturn(null)
		verifyRedirect("index.html") {
			verify(core).createSone(ownIdentities_[2])
			verify(webInterface).setCurrentSone(toadletContext, null)
		}
	}

	@Test
	fun `create sone is not shown in menu if full access is required but client doesn’t have full access`() {
		core.preferences.isRequireFullAccess = true
		assertThat(page.isEnabled(toadletContext), equalTo(false))
	}

	@Test
	fun `create sone is shown in menu if no sone is logged in`() {
		unsetCurrentSone()
		assertThat(page.isEnabled(toadletContext), equalTo(true))
	}

	@Test
	fun `create sone is shown in menu if a single sone exists`() {
		addLocalSone("local-sone", localSones_[0])
		assertThat(page.isEnabled(toadletContext), equalTo(true))
	}

	@Test
	fun `create sone is not shown in menu if more than one sone exists`() {
		addLocalSone("local-sone1", localSones_[0])
		addLocalSone("local-sone2", localSones_[1])
		assertThat(page.isEnabled(toadletContext), equalTo(false))
	}

	@Test
	fun `create sone is shown in menu if no sone is logged in and client has full access`() {
		core.preferences.isRequireFullAccess = true
		whenever(toadletContext.isAllowedFullAccess).thenReturn(true)
		unsetCurrentSone()
		assertThat(page.isEnabled(toadletContext), equalTo(true))
	}

}
