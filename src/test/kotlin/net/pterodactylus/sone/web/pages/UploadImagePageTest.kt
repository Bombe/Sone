package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Image
import net.pterodactylus.sone.data.Image.Modifier
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.TemporaryImage
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.mockBuilder
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.eq
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [UploadImagePage].
 */
class UploadImagePageTest : WebPageTest() {

	private val parentAlbum = mock<Album>().apply {
		whenever(id).thenReturn("parent-id")
		whenever(sone).thenReturn(currentSone)
	}

	override fun getPage() = UploadImagePage(template, webInterface)

	@Test
	fun `get request does not redirect or upload anything`() {
		page.handleRequest(freenetRequest, templateContext)
		verify(core, never()).createTemporaryImage(any(), any())
		verify(core, never()).createImage(any(), any(), any())
	}

	@Test
	fun `post request without parent results in no permission error page`() {
		setMethod(POST)
		verifyRedirect("noPermission.html")
	}

	@Test
	fun `post request with parent that is not the current sone results in no permission error page`() {
		setMethod(POST)
		addHttpRequestPart("parent", "parent-id")
		whenever(parentAlbum.sone).thenReturn(mock<Sone>())
		addAlbum("parent-id", parentAlbum)
		verifyRedirect("noPermission.html")
	}

	@Test
	fun `post request with empty name redirects to error page`() {
		setMethod(POST)
		addAlbum("parent-id", parentAlbum)
		addHttpRequestPart("parent", "parent-id")
		addHttpRequestPart("title", " ")
		verifyRedirect("emptyImageTitle.html")
	}

	@Test
	fun `uploading an invalid image results in no redirect and message set in template context`() {
		setMethod(POST)
		addAlbum("parent-id", parentAlbum)
		addHttpRequestPart("parent", "parent-id")
		addHttpRequestPart("title", "title")
		addUploadedFile("image", "image.png", "image/png", "no-image.png")
		page.handleRequest(freenetRequest, templateContext)
		verify(core, never()).createTemporaryImage(any(), any())
		assertThat(templateContext["messages"] as String?, equalTo<String>("Page.UploadImage.Error.InvalidImage"))
	}

	@Test
	fun `uploading a valid image uploads image and redirects to album browser`() {
		setMethod(POST)
		addAlbum("parent-id", parentAlbum)
		addHttpRequestPart("parent", "parent-id")
		addHttpRequestPart("title", "Title")
		addHttpRequestPart("description", "Description")
		addUploadedFile("image", "image.png", "image/png", "image.png")
		val temporaryImage = TemporaryImage("temp-image")
		val imageModifier = mockBuilder<Modifier>()
		val image = mock<Image>().apply {
			whenever(modify()).thenReturn(imageModifier)
		}
		whenever(core.createTemporaryImage(eq("image/png"), any())).thenReturn(temporaryImage)
		whenever(core.createImage(currentSone, parentAlbum, temporaryImage)).thenReturn(image)
		verifyRedirect("imageBrowser.html?album=parent-id") {
			verify(image).modify()
			verify(imageModifier).setWidth(2)
			verify(imageModifier).setHeight(1)
			verify(imageModifier).setTitle("Title")
			verify(imageModifier).setDescription("Description")
			verify(imageModifier).update()
		}
	}

}
