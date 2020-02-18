package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.impl.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.verify

/**
 * Unit test for [DeleteAlbumPage].
 */
class DeleteAlbumPageTest : WebPageTest(::DeleteAlbumPage) {

	private val album = AlbumImpl(currentSone, "album-id")
	private val parentAlbum = AlbumImpl(currentSone, "parent-id").also { it.addAlbum(album) }

	@Before
	fun setupAlbums() {
		whenever(currentSone.id).thenReturn("sone-id")
		whenever(currentSone.isLocal).thenReturn(true)
		whenever(currentSone.rootAlbum).thenReturn(parentAlbum)
	}

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("deleteAlbum.html"))
	}

	@Test
	fun `page requires login`() {
		assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `get request with invalid album ID results in redirect to invalid page`() {
		whenever(core.getAlbum(anyString())).thenReturn(null)
		verifyRedirect("invalid.html")
	}

	@Test
	fun `get request with valid album ID sets album in template context`() {
		addAlbum("album-id", album)
		addHttpRequestParameter("album", "album-id")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["album"], equalTo<Any>(album))
	}

	@Test
	fun `post request redirects to invalid page if album is invalid`() {
		setMethod(POST)
		verifyRedirect("invalid.html")
	}

	@Test
	fun `post request redirects to no permissions page if album is not local`() {
		setMethod(POST)
		whenever(currentSone.isLocal).thenReturn(false)
		addAlbum("album-id", album)
		addHttpRequestPart("album", "album-id")
		verifyRedirect("noPermission.html")
	}

	@Test
	fun `post request with abort delete parameter set redirects to album browser`() {
		setMethod(POST)
		addAlbum("album-id", album)
		addHttpRequestPart("album", "album-id")
		addHttpRequestPart("abortDelete", "true")
		verifyRedirect("imageBrowser.html?album=album-id")
	}

	@Test
	fun `album is deleted and page redirects to sone if parent album is root album`() {
		setMethod(POST)
		addAlbum("album-id", album)
		addHttpRequestPart("album", "album-id")
		verifyRedirect("imageBrowser.html?sone=sone-id") {
			verify(core).deleteAlbum(album)
		}
	}

	@Test
	fun `album is deleted and page redirects to album if parent album is not root album`() {
		setMethod(POST)
		val subAlbum = AlbumImpl(currentSone, "sub-album-id")
		album.addAlbum(subAlbum)
		addAlbum("sub-album-id", subAlbum)
		addHttpRequestPart("album", "sub-album-id")
		verifyRedirect("imageBrowser.html?album=album-id") {
			verify(core).deleteAlbum(subAlbum)
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<DeleteAlbumPage>(), notNullValue())
	}

	@Test
	fun `page is annotated with correct template path`() {
		assertThat(page.templatePath, equalTo("/templates/deleteAlbum.html"))
	}

}
