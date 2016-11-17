package net.pterodactylus.sone.template

import freenet.support.api.HTTPRequest
import net.pterodactylus.sone.Matchers.matchesRegex
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.TemplateContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import java.net.URI

/**
 * Unit test for [RequestChangeFilter].
 */
class RequestChangeFilterTest {

	private val filter = RequestChangeFilter()
	private val templateContext = mock<TemplateContext>()
	private val freenetRequest = mock<FreenetRequest>()
	private val httpRequest = mock<HTTPRequest>()
	private val parameters = mutableMapOf<String, String>()

	@Before
	fun setupFreenetRequest() {
		whenever(freenetRequest.httpRequest).thenReturn(httpRequest)
		whenever(freenetRequest.httpRequest.parameterNames).thenAnswer { parameters.keys }
		whenever(freenetRequest.httpRequest.getParam(anyString())).thenAnswer { parameters[it.arguments[0]] }
	}

	@Test
	fun `filter correctly appends parameter to request URL without parameters`() {
		whenever(freenetRequest.uri).thenReturn(URI("/some/path.html"))
		val uri = filter.format(templateContext, freenetRequest, mapOf("name" to "name", "value" to "value")) as URI
		assertThat(uri, equalTo(URI("/some/path.html?name=value")))
	}

	@Test
	fun `filter cuts off old query`() {
		whenever(freenetRequest.uri).thenReturn(URI("/some/path.html?foo=bar"))
		val uri = filter.format(templateContext, freenetRequest, mapOf("name" to "name", "value" to "value")) as URI
		assertThat(uri, equalTo(URI("/some/path.html?name=value")))
	}

	@Test
	fun `filter correctly appends parameter to request URL with parameters`() {
		parameters["foo"] = "bar"
		whenever(freenetRequest.uri).thenReturn(URI("/some/path.html"))
		val uri = filter.format(templateContext, freenetRequest, mapOf("name" to "name", "value" to "value")) as URI
		assertThat(uri.toString(), matchesRegex("/some/path.html\\?(foo=bar&name=value|name=value&foo=bar)"))
	}

	@Test
	fun `filter overwrites existing parameter value`() {
		parameters["name"] = "old"
		whenever(freenetRequest.uri).thenReturn(URI("/some/path.html"))
		val uri = filter.format(templateContext, freenetRequest, mapOf("name" to "name", "value" to "value")) as URI
		assertThat(uri, equalTo(URI("/some/path.html?name=value")))
	}

	@Test
	fun `filter correctly encodes characters`() {
		whenever(freenetRequest.uri).thenReturn(URI("/some/path.html"))
		val uri = filter.format(templateContext, freenetRequest, mapOf("name" to "name", "value" to " välue")) as URI
		assertThat(uri, equalTo(URI("/some/path.html?name=+v%C3%A4lue")))
	}

}
