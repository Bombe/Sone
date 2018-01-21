package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.TemporaryImage
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.web.baseInjector
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test

/**
 * Unit test for [GetImagePage].
 */
class GetImagePageTest {

	private val scaffolding = WebPageTest()
	private val page = GetImagePage(scaffolding.webInterface)
	private val freenetRequest = scaffolding.freenetRequest
	private val response = scaffolding.response
	private val responseContent = scaffolding.responseContent

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo("getImage.html"))
	}

	@Test
	fun `page is not a prefix page`() {
		assertThat(page.isPrefixPage, equalTo(false))
	}

	@Test
	fun `page is not link-excepted`() {
		assertThat(page.isLinkExcepted(null), equalTo(false))
	}

	@Test
	fun `invalid image returns 404 response`() {
		page.handleRequest(freenetRequest, response)
		assertThat(response.statusCode, equalTo(404))
		assertThat(response.statusText, equalTo("Not found."))
		assertThat(response.contentType, equalTo("text/html; charset=utf-8"))
		assertThat(responseContent.toByteArray(), equalTo(ByteArray(0)))
	}

	@Test
	fun `valid image returns response with correct data`() {
		val image = TemporaryImage("temp-id").apply {
			mimeType = "image/test"
			imageData = ByteArray(5, Int::toByte)
		}
		scaffolding.addHttpRequestParameter("image", "temp-id")
		scaffolding.addTemporaryImage("temp-id", image)
		page.handleRequest(freenetRequest, response)
		assertThat(response.statusCode, equalTo(200))
		assertThat(response.contentType, equalTo("image/test"))
		assertThat(responseContent.toByteArray(), equalTo(ByteArray(5, Int::toByte)))
		println(response.headers.map { it.name to it.iterator().asSequence().toList() })
		assertThat(response.headers.map { it.name to it.iterator().asSequence().toList() }, contains(
				"Content-Disposition" to listOf("attachment; filename=temp-id.test")
		))
	}

	@Test
	fun `page can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<GetImagePage>(), notNullValue())
	}

}
