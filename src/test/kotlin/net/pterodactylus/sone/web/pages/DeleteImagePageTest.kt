package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [DeleteImagePage].
 */
class DeleteImagePageTest: WebPageTest(::DeleteImagePage) {

	private val image = mock<Image>()
	private val sone = mock<Sone>()

	@Before
	fun setupImage() {
		val album = mock<Album>()
		whenever(album.id).thenReturn("album-id")
		whenever(image.id).thenReturn("image-id")
		whenever(image.sone).thenReturn(sone)
		whenever(image.album).thenReturn(album)
		whenever(sone.isLocal).thenReturn(true)
	}

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("deleteImage.html"))
	}

	@Test
	fun `page requires login`() {
		assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `get request with invalid image redirects to invalid page`() {
		verifyRedirect("invalid.html")
	}

	@Test
	fun `get request with image from non-local sone redirects to no permissions page`() {
		whenever(sone.isLocal).thenReturn(false)
		addImage("image-id", image)
		addHttpRequestParameter("image", "image-id")
		verifyRedirect("noPermission.html")
	}

	@Test
	fun `get request with image from local sone sets image in template context`() {
		addImage("image-id", image)
		addHttpRequestParameter("image", "image-id")
		page.processTemplate(freenetRequest, templateContext)
		assertThat(templateContext["image"], equalTo<Any>(image))
	}

	@Test
	fun `post request with abort delete flag set redirects to image browser`() {
		setMethod(POST)
		addImage("image-id", image)
		addHttpRequestPart("image", "image-id")
		addHttpRequestPart("abortDelete", "true")
		verifyRedirect("imageBrowser.html?image=image-id")
	}

	@Test
	fun `post request deletes image and redirects to image browser`() {
		setMethod(POST)
		addImage("image-id", image)
		addHttpRequestPart("image", "image-id")
		verifyRedirect("imageBrowser.html?album=album-id") {
			verify(webInterface.core).deleteImage(image)
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<DeleteImagePage>(), notNullValue())
	}

	@Test
	fun `page is annotated with correct template path`() {
	    assertThat(page.templatePath, equalTo("/templates/deleteImage.html"))
	}

}
