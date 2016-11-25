package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Album.Modifier.AlbumTitleMustNotBeEmpty
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.mockBuilder
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.WebTestUtils.redirectsTo
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit test for [EditAlbumPage].
 */
class EditAlbumPageTest : WebPageTest() {

	private val page = EditAlbumPage(template, webInterface)

	private val album = mock<Album>()
	private val parentAlbum = mock<Album>()
	private val modifier = mockBuilder<Album.Modifier>()
	private val sone = mock<Sone>()

	@Before
	fun setup() {
		whenever(album.id).thenReturn("album-id")
		whenever(album.sone).thenReturn(sone)
		whenever(album.parent).thenReturn(parentAlbum)
		whenever(album.modify()).thenReturn(modifier)
		whenever(modifier.update()).thenReturn(album)
		whenever(parentAlbum.id).thenReturn("parent-id")
		whenever(sone.isLocal).thenReturn(true)
		addHttpRequestHeader("Host", "www.te.st")
	}

	@Test
	fun `get request does not redirect`() {
		request("", GET)
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request with invalid album redirects to invalid page`() {
		request("", POST)
		expectedException.expect(redirectsTo("invalid.html"))
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request with album of non-local sone redirects to no permissions page`() {
		request("", POST)
		whenever(sone.isLocal).thenReturn(false)
		addAlbum("album-id", album)
		addHttpRequestParameter("album", "album-id")
		expectedException.expect(redirectsTo("noPermission.html"))
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request with move left requested moves album to the left and redirects to album browser`() {
		request("", POST)
		addAlbum("album-id", album)
		addHttpRequestParameter("album", "album-id")
		addHttpRequestParameter("moveLeft", "true")
		expectedException.expect(redirectsTo("imageBrowser.html?album=parent-id"))
		try {
			page.handleRequest(freenetRequest, templateContext)
		} finally {
			verify(parentAlbum).moveAlbumUp(album)
			verify(core).touchConfiguration()
		}
	}

	@Test
	fun `post request with move right requested moves album to the left and redirects to album browser`() {
		request("", POST)
		addAlbum("album-id", album)
		addHttpRequestParameter("album", "album-id")
		addHttpRequestParameter("moveRight", "true")
		expectedException.expect(redirectsTo("imageBrowser.html?album=parent-id"))
		try {
			page.handleRequest(freenetRequest, templateContext)
		} finally {
			verify(parentAlbum).moveAlbumDown(album)
			verify(core).touchConfiguration()
		}
	}

	@Test
	fun `post request with empty album title redirects to empty album title page`() {
		request("", POST)
		addAlbum("album-id", album)
		addHttpRequestParameter("album", "album-id")
		whenever(modifier.setTitle("")).thenThrow(AlbumTitleMustNotBeEmpty())
		expectedException.expect(redirectsTo("emptyAlbumTitle.html"))
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request with non-empty album title and description redirects to album browser`() {
		request("", POST)
		addAlbum("album-id", album)
		addHttpRequestParameter("album", "album-id")
		addHttpRequestParameter("title", "title")
		addHttpRequestParameter("description", "description")
		expectedException.expect(redirectsTo("imageBrowser.html?album=album-id"))
		try {
			page.handleRequest(freenetRequest, templateContext)
		} finally {
			verify(modifier).setTitle("title")
			verify(modifier).setDescription("description")
			verify(modifier).update()
			verify(core).touchConfiguration()
		}
	}

}
