package net.pterodactylus.sone.web.page

import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.util.web.*
import net.pterodactylus.util.web.Method.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*
import java.io.*
import java.nio.charset.StandardCharsets.*

class FreenetTemplatePageTest {

	private val templateRenderer = deepMock<TemplateRenderer>()
	private val loaders = mock<Loaders>()
	private val page = TestPage(templateRenderer, loaders)

	@Test
	fun `path is exposed correctly`() {
		assertThat(page.path, equalTo("/test/path"))
	}

	@Test
	fun `getPageTitle() default implementation returns empty string`() {
		assertThat(page.getPageTitle(mock()), equalTo(""))
	}

	@Test
	fun `isPrefixPage() default implementation returns false`() {
		assertThat(page.isPrefixPage, equalTo(false))
	}

	@Test
	fun `getStylesheets() default implementation returns empty collection`() {
		assertThat(page.styleSheets, empty())
	}

	@Test
	fun `getShortcutIcon() default implementation returns null`() {
		assertThat(page.shortcutIcon, nullValue())
	}

	@Test
	fun `getRedirectTarget() default implementation returns null`() {
		assertThat(page.getRedirectTarget(mock()), nullValue())
	}

	@Test
	fun `getAdditionalLinkNodes() default implementation returns empty collection`() {
		assertThat(page.getAdditionalLinkNodes(mock()), empty())
	}

	@Test
	fun `isFullAccessOnly() default implementation returns false`() {
		assertThat(page.isFullAccessOnly, equalTo(false))
	}

	@Test
	fun `isLinkExcepted() default implementation returns false`() {
		assertThat(page.isLinkExcepted(mock()), equalTo(false))
	}

	@Test
	fun `isEnabled() returns true if full access only is false`() {
		assertThat(page.isEnabled(mock()), equalTo(true))
	}

	@Test
	fun `isEnabled() returns false if full access only is true`() {
		val page = object : TestPage(templateRenderer, loaders) {
			override val isFullAccessOnly = true
		}
		assertThat(page.isEnabled(mock()), equalTo(false))
	}

	@Test
	fun `page with redirect target throws redirect exception on handleRequest`() {
		val page = object : TestPage(templateRenderer, loaders) {
			override fun getRedirectTarget(request: FreenetRequest) = "foo"
		}
		val request = mock<FreenetRequest>()
		val response = mock<Response>()
		val pageResponse = page.handleRequest(request, response)
		assertThat(pageResponse.statusCode, anyOf(equalTo(302), equalTo(307)))
		assertThat(pageResponse.headers, contains(hasHeader("location", "foo")))
	}

	@Test
	fun `page with full access only returns unauthorized on handleRequest with non-full access request`() {
		val page = object : TestPage(templateRenderer, loaders) {
			override val isFullAccessOnly = true
		}
		val request = deepMock<FreenetRequest>()
		val response = Response(null)
		val pageResponse = page.handleRequest(request, response)
		assertThat(pageResponse.statusCode, equalTo(401))
	}

	@Test
	fun `page redirects on POST without form password`() {
		val request = deepMock<FreenetRequest>().apply {
			whenever(httpRequest.getPartAsStringFailsafe(any(), anyInt())).thenReturn("")
			whenever(method).thenReturn(POST)
		}
		val response = Response(null)
		val pageResponse = page.handleRequest(request, response)
		assertThat(pageResponse.statusCode, anyOf(equalTo(302), equalTo(307)))
		assertThat(pageResponse.headers, contains(hasHeader("location", "invalid-form-password")))
	}

	@Test
	fun `page redirects on POST with invalid password`() {
		val request = deepMock<FreenetRequest>().apply {
			whenever(httpRequest.getPartAsStringFailsafe(any(), anyInt())).thenReturn("invalid")
			whenever(method).thenReturn(POST)
		}
		val response = Response(null)
		val pageResponse = page.handleRequest(request, response)
		assertThat(pageResponse.statusCode, anyOf(equalTo(302), equalTo(307)))
		assertThat(pageResponse.headers, contains(hasHeader("location", "invalid-form-password")))
	}

	@Test
	@Dirty
	fun `freenet template page creates page with correct title`() {
		val page = object : TestPage(templateRenderer, loaders) {
			override fun getPageTitle(request: FreenetRequest) = "page title"
		}
		val request = deepMock<FreenetRequest>()
		val pageMakerInteractionFactory = deepMock<PageMakerInteractionFactory>()
		whenever(pageMakerInteractionFactory.createPageMaker(request.toadletContext, "page title").renderPage()).thenReturn("<page>")
		setField(page, "pageMakerInteractionFactory", pageMakerInteractionFactory)
		val response = page.handleRequest(request, Response(ByteArrayOutputStream()))
		assertThat(response.statusCode, equalTo(200))
		assertThat((response.content as ByteArrayOutputStream).toString(UTF_8.name()), equalTo("<page>"))
	}

	@Test
	fun `template from annotation is loaded`() {
		verify(loaders).loadTemplate("template-path")
	}

	@TemplatePath("template-path")
	@ToadletPath("/test/path")
	private open class TestPage(templateRenderer: TemplateRenderer, loaders: Loaders) : FreenetTemplatePage(templateRenderer, loaders, "invalid-form-password")

}
