package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method
import org.junit.Test

/**
 * Unit test for [UploadImagePage].
 */
class UploadImagePageTest : WebPageTest() {

	private val parentAlbum = mock<Album>().apply {
		whenever(sone).thenReturn(currentSone)
	}

	override fun getPage() = UploadImagePage(template, webInterface)

	@Test
	fun `post request with empty name redirects to error page`() {
		request("", Method.POST)
		addAlbum("parent-id", parentAlbum)
		addHttpRequestParameter("parent", "parent-id")
		addHttpRequestParameter("title", " ")
		verifyRedirect("emptyImageTitle.html")
	}

}
