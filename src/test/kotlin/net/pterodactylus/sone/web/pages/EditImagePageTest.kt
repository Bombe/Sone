package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.data.impl.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [EditImagePage].
 */
class EditImagePageTest : WebPageTest(::EditImagePage) {

	private val sone = mock<Sone>()
	private val image = ImageImpl("image-id").modify().setSone(sone).update()!!
	private val album = AlbumImpl(sone, "album-id").also {
		it.addImage(ImageImpl("1").modify().setSone(sone).update())
		it.addImage(image)
		it.addImage(ImageImpl("2").modify().setSone(sone).update())
	}

	@Before
	fun setupImage() {
		whenever(sone.isLocal).thenReturn(true)
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
		addTranslation("Page.EditImage.Title", "edit image page title")
		assertThat(page.getPageTitle(soneRequest), equalTo("edit image page title"))
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
			assertThat(album.images.indexOf(image), equalTo(0))
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
			assertThat(album.images.indexOf(image), equalTo(2))
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
			assertThat(image.title, equalTo("Title"))
			assertThat(image.description, equalTo("Description"))
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
			assertThat(image.title, equalTo("Title"))
			assertThat(image.description, equalTo("Get KSK@GPL.txt"))
			verify(core).touchConfiguration()
		}
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<EditImagePage>(), notNullValue())
	}

}
