package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.junit.rules.*
import java.nio.file.*
import kotlin.text.Charsets.UTF_8

/**
 * Unit test for [ReloadingPage].
 */
class ReloadingPageTest {

	@Rule
	@JvmField
	val tempFolder = TemporaryFolder()
	private val folder by lazy { tempFolder.newFolder()!! }
	private val page by lazy { ReloadingPage<FreenetRequest>("/prefix/", folder.path, "text/plain") }
	private val webPageTest = WebPageTest()
	private val freenetRequest = webPageTest.freenetRequest
	private val responseBytes = webPageTest.responseContent
	private val response = webPageTest.response

	@Test
	fun `page returns correct path prefix`() {
		assertThat(page.path, equalTo("/prefix/"))
	}

	@Test
	fun `page returns that it’s a prefix page`() {
		assertThat(page.isPrefixPage, equalTo(true))
	}

	@Test
	fun `requesting invalid file results in 404`() {
		webPageTest.request("/prefix/path/file.txt")
		page.handleRequest(freenetRequest, response)
		assertThat(response.statusCode, equalTo(404))
		assertThat(response.statusText, equalTo("Not found"))
	}

	@Test
	fun `requesting valid file results in 200 and delivers file`() {
		Files.write(Paths.get(folder.path, "file.txt"), listOf("Hello", "World"), UTF_8)
		webPageTest.request("/prefix/path/file.txt")
		page.handleRequest(freenetRequest, response)
		assertThat(response.statusCode, equalTo(200))
		assertThat(response.statusText, equalTo("OK"))
		assertThat(response.contentType, equalTo("text/plain"))
		assertThat(responseBytes.toByteArray(), equalTo("Hello\nWorld\n".toByteArray()))
	}

	@Test
	fun `page can be created by dependency injection`() {
		assertThat(baseInjector.getInstance<ReloadingPage<*>>(), notNullValue())
	}

}
