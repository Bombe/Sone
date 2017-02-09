package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Album.Modifier.AlbumTitleMustNotBeEmpty
import net.pterodactylus.sone.test.deepMock
import net.pterodactylus.sone.test.selfMock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [CreateAlbumPage].
 */
class CreateAlbumPageTest: WebPageTest() {

	private val page = CreateAlbumPage(template, webInterface)

	override fun getPage() = page

	private val parentAlbum = createAlbum("parent-id")
	private val newAlbum = createAlbum("album-id")

	@Before
	fun setupAlbums() {
		whenever(core.createAlbum(currentSone, parentAlbum)).thenReturn(newAlbum)
		whenever(currentSone.rootAlbum).thenReturn(parentAlbum)
	}

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("createAlbum.html"))
	}

	@Test
	fun `get request shows template`() {
		page.processTemplate(freenetRequest, templateContext)
	}

	@Test
	fun `missing name results in attribute being set in template context`() {
		request("", POST)
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["nameMissing"], equalTo<Any>(true))
	}

	private fun createAlbum(albumId: String) = deepMock<Album>().apply {
		whenever(id).thenReturn(albumId)
		selfMock<Album.Modifier>().let { modifier ->
			whenever(modifier.update()).thenReturn(this@apply)
			whenever(this@apply.modify()).thenReturn(modifier)
		}
	}

	@Test
	fun `title and description are set correctly on the album`() {
		request("", POST)
		addAlbum("parent-id", parentAlbum)
		addHttpRequestParameter("name", "new name")
		addHttpRequestParameter("description", "new description")
		addHttpRequestParameter("parent", "parent-id")
		verifyRedirect("imageBrowser.html?album=album-id") {
			verify(newAlbum).modify()
			verify(newAlbum.modify()).setTitle("new name")
			verify(newAlbum.modify()).setDescription("new description")
			verify(newAlbum.modify()).update()
			verify(core).touchConfiguration()
		}
	}

	@Test
	fun `root album is used if no parent is specified`() {
		request("", POST)
		addHttpRequestParameter("name", "new name")
		addHttpRequestParameter("description", "new description")
		verifyRedirect("imageBrowser.html?album=album-id")
	}

	@Test
	fun `empty album title redirects to error page`() {
		request("", POST)
		whenever(newAlbum.modify().update()).thenThrow(AlbumTitleMustNotBeEmpty::class.java)
		addHttpRequestParameter("name", "new name")
		addHttpRequestParameter("description", "new description")
		verifyRedirect("emptyAlbumTitle.html")
	}

	@Test
	fun `album description is filtered`() {
		request("", POST)
		addHttpRequestParameter("name", "new name")
		addHttpRequestParameter("description", "new http://localhost:12345/KSK@foo description")
		addHttpRequestHeader("Host", "localhost:12345")
		verifyRedirect("imageBrowser.html?album=album-id") {
			verify(newAlbum.modify()).setDescription("new KSK@foo description")
		}
	}

}
