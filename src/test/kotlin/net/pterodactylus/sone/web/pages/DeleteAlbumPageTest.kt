package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.baseInjector
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.verify

/**
 * Unit test for [DeleteAlbumPage].
 */
class DeleteAlbumPageTest: WebPageTest(::DeleteAlbumPage) {

	private val sone = mock<Sone>()
	private val album = mock<Album>()
	private val parentAlbum = mock<Album>()

	@Before
	fun setupAlbums() {
		whenever(sone.id).thenReturn("sone-id")
		whenever(sone.isLocal).thenReturn(true)
		whenever(parentAlbum.id).thenReturn("parent-id")
		whenever(parentAlbum.isRoot).thenReturn(true)
		whenever(album.id).thenReturn("album-id")
		whenever(album.sone).thenReturn(sone)
		whenever(album.parent).thenReturn(parentAlbum)
		whenever(sone.rootAlbum).thenReturn(parentAlbum)
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
		val album = mock<Album>()
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
		whenever(sone.isLocal).thenReturn(false)
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
		whenever(parentAlbum.isRoot).thenReturn(false)
		whenever(sone.rootAlbum).thenReturn(mock())
		addAlbum("album-id", album)
		addHttpRequestPart("album", "album-id")
		verifyRedirect("imageBrowser.html?album=parent-id") {
			verify(core).deleteAlbum(album)
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<DeleteAlbumPage>(), notNullValue())
	}

}
