package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Image
import net.pterodactylus.sone.data.Image.Modifier
import net.pterodactylus.sone.data.Image.Modifier.ImageTitleMustNotBeEmpty
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.doThrow
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.mockBuilder
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method.POST
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [EditImagePage].
 */
class EditImagePageTest : WebPageTest() {

	private val page = EditImagePage(template, webInterface)

	private val image = mock<Image>()
	private val modifier = mockBuilder<Modifier>()
	private val sone = mock<Sone>()
	private val album = mock<Album>()

	override fun getPage() = page

	@Before
	fun setupImage() {
		whenever(sone.isLocal).thenReturn(true)
		whenever(album.id).thenReturn("album-id")
		whenever(modifier.update()).thenReturn(image)
		whenever(image.sone).thenReturn(sone)
		whenever(image.album).thenReturn(album)
		whenever(image.modify()).thenReturn(modifier)
	}

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("editImage.html"))
	}

	@Test
	fun `page requires login`() {
	    assertThat(page.requiresLogin(), equalTo(true))
	}

	@Test
	fun `page returns correct title`() {
		whenever(l10n.getString("Page.EditImage.Title")).thenReturn("edit image page title")
	    assertThat(page.getPageTitle(freenetRequest), equalTo("edit image page title"))
	}

	@Test
	fun `get request does not redirect`() {
		page.processTemplate(freenetRequest, templateContext)
	}

	@Test
	fun `post request with invalid image redirects to invalid page`() {
		setMethod(POST)
		verifyRedirect("invalid.html")
	}

	@Test
	fun `post request with valid image from non-local sone redirects to no permission page`() {
		setMethod(POST)
		whenever(sone.isLocal).thenReturn(false)
		addImage("image-id", image)
		addHttpRequestPart("image", "image-id")
		verifyRedirect("noPermission.html")
	}

	@Test
	fun `post request with valid image and move left requested moves image left and redirects to return page`() {
		setMethod(POST)
		addImage("image-id", image)
		addHttpRequestPart("image", "image-id")
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("moveLeft", "true")
		verifyRedirect("return.html") {
			verify(album).moveImageUp(image)
			verify(core).touchConfiguration()
		}
	}

	@Test
	fun `post request with valid image and move right requested moves image right and redirects to return page`() {
		setMethod(POST)
		addImage("image-id", image)
		addHttpRequestPart("image", "image-id")
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("moveRight", "true")
		verifyRedirect("return.html") {
			verify(album).moveImageDown(image)
			verify(core).touchConfiguration()
		}
	}

	@Test
	fun `post request with valid image but only whitespace in the title redirects to empty image title page`() {
		setMethod(POST)
		addImage("image-id", image)
		addHttpRequestPart("image", "image-id")
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("title", "   ")
		whenever(modifier.update()).doThrow<ImageTitleMustNotBeEmpty>()
		verifyRedirect("emptyImageTitle.html") {
			verify(core, never()).touchConfiguration()
		}
	}

	@Test
	fun `post request with valid image title and description modifies image and redirects to reutrn page`() {
		setMethod(POST)
		addImage("image-id", image)
		addHttpRequestPart("image", "image-id")
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("title", "Title")
		addHttpRequestPart("description", "Description")
		verifyRedirect("return.html") {
			verify(modifier).setTitle("Title")
			verify(modifier).setDescription("Description")
			verify(modifier).update()
			verify(core).touchConfiguration()
		}
	}

	@Test
	fun `post request with image title and description modifies image with filtered description and redirects to return page`() {
		setMethod(POST)
		addImage("image-id", image)
		addHttpRequestPart("image", "image-id")
		addHttpRequestPart("returnPage", "return.html")
		addHttpRequestPart("title", "Title")
		addHttpRequestHeader("Host", "www.te.st")
		addHttpRequestPart("description", "Get http://www.te.st/KSK@GPL.txt")
		verifyRedirect("return.html") {
			verify(modifier).setTitle("Title")
			verify(modifier).setDescription("Get KSK@GPL.txt")
			verify(modifier).update()
			verify(core).touchConfiguration()
		}
	}

}
