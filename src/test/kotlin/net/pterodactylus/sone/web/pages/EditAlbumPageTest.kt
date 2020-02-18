package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.impl.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [EditAlbumPage].
 */
class EditAlbumPageTest : WebPageTest(::EditAlbumPage) {

	private val album = AlbumImpl(currentSone, "album-id")
	private val parentAlbum = AlbumImpl(currentSone, "parent-id").also {
		it.addAlbum(AlbumImpl(currentSone))
		it.addAlbum(album)
		it.addAlbum(AlbumImpl(currentSone))
	}

	@Before
	fun setup() {
		whenever(currentSone.isLocal).thenReturn(true)
		addHttpRequestHeader("Host", "www.te.st")
	}

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("editAlbum.html"))
	}

	@Test
	fun `page requires login`() {
		assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `page returns correct title`() {
		addTranslation("Page.EditAlbum.Title", "edit album page")
		assertThat(page.getPageTitle(soneRequest), equalTo("edit album page"))
	}

	@Test
	fun `get request does not redirect`() {
		page.processTemplate(freenetRequest, templateContext)
	}

	@Test
	fun `post request with invalid album redirects to invalid page`() {
		setMethod(POST)
		verifyRedirect("invalid.html")
	}

	@Test
	fun `post request with album of non-local sone redirects to no permissions page`() {
		setMethod(POST)
		whenever(currentSone.isLocal).thenReturn(false)
		addAlbum("album-id", album)
		addHttpRequestPart("album", "album-id")
		verifyRedirect("noPermission.html")
	}

	@Test
	fun `post request with move left requested moves album to the left and redirects to album browser`() {
		setMethod(POST)
		addAlbum("album-id", album)
		addHttpRequestPart("album", "album-id")
		addHttpRequestPart("moveLeft", "true")
		verifyRedirect("imageBrowser.html?album=parent-id") {
			assertThat(parentAlbum.albums.indexOf(album), equalTo(0))
			verify(core).touchConfiguration()
		}
	}

	@Test
	fun `post request with move right requested moves album to the left and redirects to album browser`() {
		setMethod(POST)
		addAlbum("album-id", album)
		addHttpRequestPart("album", "album-id")
		addHttpRequestPart("moveRight", "true")
		verifyRedirect("imageBrowser.html?album=parent-id") {
			assertThat(parentAlbum.albums.indexOf(album), equalTo(2))
			verify(core).touchConfiguration()
		}
	}

	@Test
	fun `post request with empty album title redirects to empty album title page`() {
		setMethod(POST)
		addAlbum("album-id", album)
		addHttpRequestPart("album", "album-id")
		verifyRedirect("emptyAlbumTitle.html")
	}

	@Test
	fun `post request with non-empty album title and description redirects to album browser`() {
		setMethod(POST)
		addAlbum("album-id", album)
		addHttpRequestPart("album", "album-id")
		addHttpRequestPart("title", "title")
		addHttpRequestPart("description", "description")
		verifyRedirect("imageBrowser.html?album=album-id") {
			assertThat(album.title, equalTo("title"))
			assertThat(album.description, equalTo("description"))
			verify(core).touchConfiguration()
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<EditAlbumPage>(), notNullValue())
	}

}
