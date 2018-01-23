package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Album.Modifier.AlbumTitleMustNotBeEmpty
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.impl.AlbumImpl
import net.pterodactylus.sone.test.deepMock
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.baseInjector
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test

/**
 * Unit test for [EditAlbumAjaxPage].
 */
class EditAlbumAjaxPageTest : JsonPageTest("editAlbum.ajax", pageSupplier = ::EditAlbumAjaxPage) {

	private val sone = mock<Sone>()
	private val localSone = mock<Sone>().apply { whenever(isLocal).thenReturn(true) }
	private val album = mock<Album>().apply { whenever(id).thenReturn("album-id") }

	@Test
	fun `request without album results in invalid-album-id`() {
		assertThatJsonFailed("invalid-album-id")
	}

	@Test
	fun `request with non-local album results in not-authorized`() {
		whenever(album.sone).thenReturn(sone)
		addAlbum(album)
		addRequestParameter("album", "album-id")
		assertThatJsonFailed("not-authorized")
	}

	@Test
	fun `request with moveLeft moves album to the left`() {
		whenever(album.sone).thenReturn(localSone)
		val swappedAlbum = mock<Album>().apply { whenever(id).thenReturn("swapped") }
		val parentAlbum = mock<Album>()
		whenever(parentAlbum.moveAlbumUp(album)).thenReturn(swappedAlbum)
		whenever(album.parent).thenReturn(parentAlbum)
		addAlbum(album)
		addRequestParameter("album", "album-id")
		addRequestParameter("moveLeft", "true")
		assertThatJsonIsSuccessful()
		assertThat(json["sourceAlbumId"]?.asText(), equalTo("album-id"))
		assertThat(json["destinationAlbumId"]?.asText(), equalTo("swapped"))
	}

	@Test
	fun `request with moveRight moves album to the right`() {
		whenever(album.sone).thenReturn(localSone)
		val swappedAlbum = mock<Album>().apply { whenever(id).thenReturn("swapped") }
		val parentAlbum = mock<Album>()
		whenever(parentAlbum.moveAlbumDown(album)).thenReturn(swappedAlbum)
		whenever(album.parent).thenReturn(parentAlbum)
		addAlbum(album)
		addRequestParameter("album", "album-id")
		addRequestParameter("moveRight", "true")
		assertThatJsonIsSuccessful()
		assertThat(json["sourceAlbumId"]?.asText(), equalTo("album-id"))
		assertThat(json["destinationAlbumId"]?.asText(), equalTo("swapped"))
	}

	@Test
	fun `request with missing title results in invalid-title`() {
		whenever(album.sone).thenReturn(localSone)
		whenever(album.modify()).thenReturn(deepMock())
		whenever(album.modify().setTitle("")).thenThrow(AlbumTitleMustNotBeEmpty::class.java)
		addAlbum(album)
		addRequestParameter("album", "album-id")
		assertThatJsonFailed("invalid-album-title")
	}

	@Test
	fun `request with title and description sets title and filtered description`() {
		val album = AlbumImpl(currentSone, "album-id")
		addAlbum(album)
		addRequestParameter("album", "album-id")
		addRequestParameter("title", "new title")
		addRequestParameter("description", "foo http://127.0.0.1:8888/KSK@foo.html link")
		addRequestHeader("Host", "127.0.0.1:8888")
		assertThatJsonIsSuccessful()
		assertThat(json["albumId"]?.asText(), equalTo("album-id"))
		assertThat(json["title"]?.asText(), equalTo("new title"))
		assertThat(json["description"]?.asText(), equalTo("foo KSK@foo.html link"))
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<EditAlbumAjaxPage>(), notNullValue())
	}

}
