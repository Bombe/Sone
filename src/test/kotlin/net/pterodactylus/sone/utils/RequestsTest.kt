package net.pterodactylus.sone.utils

import freenet.support.api.HTTPRequest
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import net.pterodactylus.util.web.Request
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.eq

/**
 * Unit test for the [Request] utilities.
 */
class RequestsTest {

	private val httpGetRequest = mock<HTTPRequest>().apply { whenever(method).thenReturn("GET") }
	private val httpPostRequest = mock<HTTPRequest>().apply { whenever(method).thenReturn("POST") }
	private val getRequest = mock<Request>().apply { whenever(method).thenReturn(GET) }
	private val postRequest = mock<Request>().apply { whenever(method).thenReturn(POST) }
	private val freenetGetRequest = mock<FreenetRequest>().apply {
		whenever(method).thenReturn(GET)
		whenever(httpRequest).thenReturn(this@RequestsTest.httpGetRequest)
	}
	private val freenetPostRequest = mock<FreenetRequest>().apply {
		whenever(method).thenReturn(POST)
		whenever(httpRequest).thenReturn(this@RequestsTest.httpPostRequest)
	}

	@Test
	fun `GET request is recognized correctly`() {
		assertThat(getRequest.isGET, equalTo(true))
		assertThat(getRequest.isPOST, equalTo(false))
	}

	@Test
	fun `POST request is recognized correctly`() {
		assertThat(postRequest.isGET, equalTo(false))
		assertThat(postRequest.isPOST, equalTo(true))
	}

	@Test
	fun `correct parameter of GET request is returned`() {
		whenever(httpGetRequest.getParam("test-param")).thenAnswer { "test-value" }
		assertThat(freenetGetRequest.parameters["test-param"], equalTo("test-value"))
	}

	@Test
	fun `correct parameter of POST request is returned`() {
		whenever(httpPostRequest.getPartAsStringFailsafe(eq("test-param"), anyInt())).thenAnswer { "test-value" }
		assertThat(freenetPostRequest.parameters["test-param"], equalTo("test-value"))
	}

	@Test
	fun `parameter of unknown request is not returned`() {
		val request = mock<FreenetRequest>()
		val httpRequest = mock<HTTPRequest>()
		whenever(request.httpRequest).thenReturn(httpRequest)
		assertThat(request.parameters["test-param"], nullValue())
	}

	@Test
	fun `parameter of GET request is checked for presence correctly`() {
		whenever(httpGetRequest.isParameterSet("test-param")).thenAnswer { true }
		assertThat("test-param" in freenetGetRequest.parameters, equalTo(true))
	}

	@Test
	fun `parameter of POST request is checked for presence correctly`() {
		whenever(httpPostRequest.isPartSet("test-param")).thenAnswer { true }
		assertThat("test-param" in freenetPostRequest.parameters, equalTo(true))
	}

	@Test
	fun `parameter of unknown request is not set`() {
		val request = mock<FreenetRequest>()
		val httpRequest = mock<HTTPRequest>()
		whenever(request.httpRequest).thenReturn(httpRequest)
		assertThat("test-param" in request.parameters, equalTo(false))
	}

}
