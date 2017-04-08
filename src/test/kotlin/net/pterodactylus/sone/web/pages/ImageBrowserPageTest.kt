package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Image
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.pages.ImageBrowserPage
import net.pterodactylus.sone.web.pages.WebPageTest
import net.pterodactylus.util.web.Method.GET
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [ImageBrowserPage].
 */
class ImageBrowserPageTest : WebPageTest() {

	private val page = ImageBrowserPage(template, webInterface)

	@Test
	fun `get request with album sets album and page in template context`() {
		request("", GET)
		val album = mock<Album>()
		addAlbum("album-id", album)
		addHttpRequestParameter("album", "album-id")
		addHttpRequestParameter("page", "5")
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["albumRequested"], equalTo<Any>(true))
		assertThat(templateContext["album"], equalTo<Any>(album))
		assertThat(templateContext["page"], equalTo<Any>("5"))
	}

	@Test
	fun `get request with image sets image in template context`() {
		request("", GET)
		val image = mock<Image>()
		addImage("image-id", image)
		addHttpRequestParameter("image", "image-id")
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["imageRequested"], equalTo<Any>(true))
		assertThat(templateContext["image"], equalTo<Any>(image))
	}

	@Test
	fun `get request with sone sets sone in template context`() {
		request("", GET)
		val sone = mock<Sone>()
		addSone("sone-id", sone)
		addHttpRequestParameter("sone", "sone-id")
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["soneRequested"], equalTo<Any>(true))
		assertThat(templateContext["sone"], equalTo<Any>(sone))
	}

	@Test
	fun `get request with mode of gallery sets albums and page in template context`() {
		request("", GET)
		val firstSone = createSone("first album", "second album")
		addSone("sone1", firstSone)
		val secondSone = createSone("third album", "fourth album")
		addSone("sone2", secondSone)
		addHttpRequestParameter("mode", "gallery")
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["galleryRequested"], equalTo<Any>(true))
		@Suppress("UNCHECKED_CAST")
		assertThat(templateContext["albums"] as Iterable<Album>, contains(
				firstSone.rootAlbum.albums[0],
				secondSone.rootAlbum.albums[1],
				firstSone.rootAlbum.albums[1],
				secondSone.rootAlbum.albums[0]
		))
	}

	private fun createSone(firstAlbumTitle: String, secondAlbumTitle: String): Sone {
		return mock<Sone>().apply {
			val rootAlbum = mock<Album>()
			val firstAlbum = mock<Album>()
			val firstImage = mock<Image>().run { whenever(isInserted).thenReturn(true); this }
			whenever(firstAlbum.images).thenReturn(listOf(firstImage))
			val secondAlbum = mock<Album>()
			val secondImage = mock<Image>().run { whenever(isInserted).thenReturn(true); this }
			whenever(secondAlbum.images).thenReturn(listOf(secondImage))
			whenever(firstAlbum.title).thenReturn(firstAlbumTitle)
			whenever(secondAlbum.title).thenReturn(secondAlbumTitle)
			whenever(rootAlbum.albums).thenReturn(listOf(firstAlbum, secondAlbum))
			whenever(this.rootAlbum).thenReturn(rootAlbum)
		}
	}

	@Test
	fun `requesting nothing will show the albums of the current sone`() {
		request("", GET)
		page.handleRequest(freenetRequest, templateContext)
		assertThat(templateContext["soneRequested"], equalTo<Any>(true))
		assertThat(templateContext["sone"], equalTo<Any>(currentSone))
	}

	@Test
	fun `page is link-excepted`() {
	    assertThat(page.isLinkExcepted(null), equalTo(true))
	}

}
