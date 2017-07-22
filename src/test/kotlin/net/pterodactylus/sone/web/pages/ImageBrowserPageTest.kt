package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Image
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [ImageBrowserPage].
 */
class ImageBrowserPageTest: WebPageTest(::ImageBrowserPage) {

	@Test
	fun `page returns correct path`() {
	    assertThat(page.path, equalTo("imageBrowser.html"))
	}

	@Test
	fun `page requires login`() {
	    assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `page returns correct title`() {
		whenever(l10n.getString("Page.ImageBrowser.Title")).thenReturn("image browser page title")
	    assertThat(page.getPageTitle(freenetRequest), equalTo("image browser page title"))
	}

	@Test
	fun `get request with album sets album and page in template context`() {
		val album = mock<Album>()
		addAlbum("album-id", album)
		addHttpRequestParameter("album", "album-id")
		addHttpRequestParameter("page", "5")
		verifyNoRedirect {
			assertThat(templateContext["albumRequested"], equalTo<Any>(true))
			assertThat(templateContext["album"], equalTo<Any>(album))
			assertThat(templateContext["page"], equalTo<Any>("5"))
		}
	}

	@Test
	fun `get request with image sets image in template context`() {
		val image = mock<Image>()
		addImage("image-id", image)
		addHttpRequestParameter("image", "image-id")
		verifyNoRedirect {
			assertThat(templateContext["imageRequested"], equalTo<Any>(true))
			assertThat(templateContext["image"], equalTo<Any>(image))
		}
	}

	@Test
	fun `get request with sone sets sone in template context`() {
		val sone = mock<Sone>()
		addSone("sone-id", sone)
		addHttpRequestParameter("sone", "sone-id")
		verifyNoRedirect {
			assertThat(templateContext["soneRequested"], equalTo<Any>(true))
			assertThat(templateContext["sone"], equalTo<Any>(sone))
		}
	}

	@Test
	fun `get request with mode of gallery sets albums and page in template context`() {
		val firstSone = createSone("first album", "second album")
		addSone("sone1", firstSone)
		val secondSone = createSone("third album", "fourth album")
		addSone("sone2", secondSone)
		addHttpRequestParameter("mode", "gallery")
		verifyNoRedirect {
			assertThat(templateContext["galleryRequested"], equalTo<Any>(true))
			@Suppress("UNCHECKED_CAST")
			assertThat(templateContext["albums"] as Iterable<Album>, contains(
					firstSone.rootAlbum.albums[0],
					secondSone.rootAlbum.albums[1],
					firstSone.rootAlbum.albums[1],
					secondSone.rootAlbum.albums[0]
			))
		}
	}

	@Test
	fun `get request for gallery can show second page`() {
		core.preferences.imagesPerPage = 2
		val firstSone = createSone("first album", "second album")
		addSone("sone1", firstSone)
		val secondSone = createSone("third album", "fourth album")
		addSone("sone2", secondSone)
		addHttpRequestParameter("mode", "gallery")
		addHttpRequestParameter("page", "1")
		verifyNoRedirect {
			assertThat(templateContext["galleryRequested"], equalTo<Any>(true))
			@Suppress("UNCHECKED_CAST")
			assertThat(templateContext["albums"] as Iterable<Album>, contains(
					firstSone.rootAlbum.albums[1],
					secondSone.rootAlbum.albums[0]
			))
		}
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
		verifyNoRedirect {
			assertThat(templateContext["soneRequested"], equalTo<Any>(true))
			assertThat(templateContext["sone"], equalTo<Any>(currentSone))
		}
	}

	@Test
	fun `page is link-excepted`() {
	    assertThat(page.isLinkExcepted(null), equalTo(true))
	}

}
