package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Image
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.mockBuilder
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.WebTestUtils.redirectsTo
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
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
	private val modifier = mockBuilder<Image.Modifier>()
	private val sone = mock<Sone>()
	private val album = mock<Album>()

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
	fun `get request does not redirect`() {
		request("", GET)
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request with invalid image redirects to invalid page`() {
		request("", POST)
		expectedException.expect(redirectsTo("invalid.html"))
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request with valid image from non-local sone redirects to no permission page`() {
		request("", POST)
		whenever(sone.isLocal).thenReturn(false)
		addImage("image-id", image)
		addHttpRequestParameter("image", "image-id")
		expectedException.expect(redirectsTo("noPermission.html"))
		page.handleRequest(freenetRequest, templateContext)
	}

	@Test
	fun `post request with valid image and move left requested moves image left and redirects to return page`() {
		request("", POST)
		addImage("image-id", image)
		addHttpRequestParameter("image", "image-id")
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("moveLeft", "true")
		expectedException.expect(redirectsTo("return.html"))
		try {
			page.handleRequest(freenetRequest, templateContext)
		} finally {
			verify(album).moveImageUp(image)
			verify(core).touchConfiguration()
		}
	}

	@Test
	fun `post request with valid image and move right requested moves image right and redirects to return page`() {
		request("", POST)
		addImage("image-id", image)
		addHttpRequestParameter("image", "image-id")
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("moveRight", "true")
		expectedException.expect(redirectsTo("return.html"))
		try {
			page.handleRequest(freenetRequest, templateContext)
		} finally {
			verify(album).moveImageDown(image)
			verify(core).touchConfiguration()
		}
	}

	@Test
	fun `post request with valid image but only whitespace in the title redirects to empty image title page`() {
		request("", POST)
		addImage("image-id", image)
		addHttpRequestParameter("image", "image-id")
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("title", "   ")
		expectedException.expect(redirectsTo("emptyImageTitle.html"))
		try {
			page.handleRequest(freenetRequest, templateContext)
		} finally {
			verify(core, never()).touchConfiguration()
		}
	}

	@Test
	fun `post request with valid image title and description modifies image and redirects to reutrn page`() {
		request("", POST)
		addImage("image-id", image)
		addHttpRequestParameter("image", "image-id")
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("title", "Title")
		addHttpRequestParameter("description", "Description")
		expectedException.expect(redirectsTo("return.html"))
		try {
			page.handleRequest(freenetRequest, templateContext)
		} finally {
			verify(modifier).setTitle("Title")
			verify(modifier).setDescription("Description")
			verify(modifier).update()
			verify(core).touchConfiguration()
		}
	}

	@Test
	fun `post request with image title and description modifies image with filtered description and redirects to reutrn page`() {
		request("", POST)
		addImage("image-id", image)
		addHttpRequestParameter("image", "image-id")
		addHttpRequestParameter("returnPage", "return.html")
		addHttpRequestParameter("title", "Title")
		addHttpRequestHeader("Host", "www.te.st")
		addHttpRequestParameter("description", "Get http://www.te.st/KSK@GPL.txt")
		expectedException.expect(redirectsTo("return.html"))
		try {
			page.handleRequest(freenetRequest, templateContext)
		} finally {
			verify(modifier).setTitle("Title")
			verify(modifier).setDescription("Get KSK@GPL.txt")
			verify(modifier).update()
			verify(core).touchConfiguration()
		}
	}

}
