package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.data.impl.AlbumImpl
import net.pterodactylus.sone.data.impl.ImageImpl
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import java.net.*

/**
 * Unit test for [ImageBrowserPage].
 */
class ImageBrowserPageTest : WebPageTest(::ImageBrowserPage) {

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
		addTranslation("Page.ImageBrowser.Title", "image browser page title")
		assertThat(page.getPageTitle(soneRequest), equalTo("image browser page title"))
	}

	@Test
	fun `get request with album sets album and page in template context`() {
		val album = AlbumImpl(currentSone, "album-id")
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
		val image = ImageImpl()
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
		core.preferences.newImagesPerPage = 2
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
			val rootAlbum = AlbumImpl(this)
			val firstAlbum = AlbumImpl(this).modify().setTitle(firstAlbumTitle).update()
			firstAlbum.addImage(ImageImpl("1").modify().setSone(this).setKey("key").update())
			val secondAlbum = AlbumImpl(this).modify().setTitle(secondAlbumTitle).update()
			secondAlbum.addImage(ImageImpl("2").modify().setSone(this).setKey("key").update())
			rootAlbum.addAlbum(firstAlbum)
			rootAlbum.addAlbum(secondAlbum)
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
		assertThat(page.isLinkExcepted(URI("")), equalTo(true))
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<ImageBrowserPage>(), notNullValue())
	}

	@Test
	fun `page is annotated with correct menuname`() {
		assertThat(page.menuName, equalTo("ImageBrowser"))
	}

	@Test
	fun `page is annotated with correct template path`() {
		assertThat(page.templatePath, equalTo("/templates/imageBrowser.html"))
	}

}
