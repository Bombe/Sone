package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.core.Preferences
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.web.Response
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicInteger

/**
 * Unit test for [JsonPage].
 */
class JsonPageBaseTest : TestObjects() {

	private var needsFormPassword = false
	private val pageCallCounter = AtomicInteger()
	private var pageResponse = { JsonReturnObject(true) }
	private val outputStream = ByteArrayOutputStream()
	private val response = Response(outputStream)

	private val page = object : JsonPage("path.html", webInterface) {

		override fun needsFormPassword() = this@JsonPageBaseTest.needsFormPassword

		override fun createJsonObject(request: FreenetRequest) =
				pageResponse().also { pageCallCounter.incrementAndGet() }

	}

	@Before
	fun setupWebInterface() {
		whenever(webInterface.core).thenReturn(core)
	}

	@Before
	fun setupCore() {
		whenever(core.preferences).thenReturn(Preferences(eventBus))
	}

	@Before
	fun setupFreenetRequest() {
		whenever(freenetRequest.toadletContext).thenReturn(toadletContext)
	}

	@Test
	fun `page returns 403 is full access is required but request is not full access`() {
		core.preferences.isRequireFullAccess = true
		page.handleRequest(freenetRequest, response)
		assertThat(response.statusCode, equalTo(403))
	}

	@Test
	fun `page returns 403 if form password is needed but not supplied`() {
		needsFormPassword = true
		page.handleRequest(freenetRequest, response)
		assertThat(response.statusCode, equalTo(403))
	}

	@Test
	fun `page returns 403 is form password is supplied but incorrect`() {
		needsFormPassword = true
		addRequestParameter("formPassword", formPassword + "_false")
		page.handleRequest(freenetRequest, response)
		assertThat(response.statusCode, equalTo(403))
	}

	@Test
	fun `page returns 200 if form password is required and correct`() {
		needsFormPassword = true
		addRequestParameter("formPassword", formPassword)
		page.handleRequest(freenetRequest, response)
		assertThat(response.statusCode, equalTo(200))
	}

	@Test
	fun `page returns 403 is login is required but current Sone is null`() {
		unsetCurrentSone()
		page.handleRequest(freenetRequest, response)
		assertThat(response.statusCode, equalTo(403))
	}

	@Test
	fun `page returns content if login is required and current Sone is set`() {
		page.handleRequest(freenetRequest, response)
		assertThat(response.statusCode, equalTo(200))
		assertThat(pageCallCounter.get(), equalTo(1))
	}

	@Test
	fun `page returns 500 if execution throws exception`() {
		pageResponse = { throw IllegalStateException() }
		page.handleRequest(freenetRequest, response)
		assertThat(response.statusCode, equalTo(500))
	}

	@Test
	fun `page returns stack trace if execution throws exception`() {
		pageResponse = { throw IllegalStateException() }
		page.handleRequest(freenetRequest, response)
		assertThat(outputStream.toString(), containsString("IllegalStateException"))
	}

	@Test
	fun `json page is not a prefix page`() {
	    assertThat(page.isPrefixPage, equalTo(false))
	}

}
