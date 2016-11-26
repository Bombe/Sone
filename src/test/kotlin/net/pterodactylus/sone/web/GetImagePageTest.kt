package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.TemporaryImage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [GetImagePage].
 */
class GetImagePageTest : WebPageTest() {

	private val page = GetImagePage(webInterface)

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
		assertThat(responseBytes, equalTo(ByteArray(0)))
	}

	@Test
	fun `valid image returns response with correct data`() {
		val image = TemporaryImage("temp-id").apply {
			mimeType = "image/test"
			imageData = ByteArray(5, Int::toByte)
		}
		addHttpRequestParameter("image", "temp-id")
		addTemporaryImage("temp-id", image)
		page.handleRequest(freenetRequest, response)
		assertThat(response.statusCode, equalTo(200))
		assertThat(response.contentType, equalTo("image/test"))
		assertThat(responseBytes, equalTo(ByteArray(5, Int::toByte)))
	}

}
