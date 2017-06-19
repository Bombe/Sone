package net.pterodactylus.sone.web.pages

import freenet.support.api.HTTPRequest
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.web.Response
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.text.Charsets.UTF_8

/**
 * Unit test for [ReloadingPage].
 */
class ReloadingPageTest {

	@Rule @JvmField val tempFolder = TemporaryFolder()
	private val folder by lazy { tempFolder.newFolder()!! }
	private val page by lazy { ReloadingPage<FreenetRequest>("/prefix/", folder.path, "text/plain") }
	private val freenetRequest = mock<FreenetRequest>()
	private val httpRequest = mock<HTTPRequest>()
	private val responseBytes = ByteArrayOutputStream()
	private val response = Response(responseBytes)

	@Test
	fun `page returns correct path prefix`() {
		assertThat(page.path, equalTo("/prefix/"))
	}

	@Test
	fun `page returns that it’s a prefix page`() {
		assertThat(page.isPrefixPage, equalTo(true))
	}

	private fun request(uri: String) {
		whenever(httpRequest.path).thenReturn(uri)
		whenever(freenetRequest.uri).thenReturn(URI(uri))
	}

	@Test
	fun `requesting invalid file results in 404`() {
		request("/prefix/path/file.txt")
		page.handleRequest(freenetRequest, response)
		assertThat(response.statusCode, equalTo(404))
		assertThat(response.statusText, equalTo("Not found"))
	}

	@Test
	fun `requesting valid file results in 200 and delivers file`() {
		Files.write(Paths.get(folder.path, "file.txt"), listOf("Hello", "World"), UTF_8)
		request("/prefix/path/file.txt")
		page.handleRequest(freenetRequest, response)
		assertThat(response.statusCode, equalTo(200))
		assertThat(response.statusText, equalTo("OK"))
		assertThat(response.contentType, equalTo("text/plain"))
		assertThat(responseBytes.toByteArray(), equalTo("Hello\nWorld\n".toByteArray()))
	}

}
