package net.pterodactylus.sone.utils

import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import net.pterodactylus.util.web.Request
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for the [Request] utilities.
 */
class RequestsTest {

	@Test
	fun `GET request is recognized correctly`() {
		val request = mock<Request>().apply { whenever(method).thenReturn(GET) }
		assertThat(request.isGET, equalTo(true))
		assertThat(request.isPOST, equalTo(false))
	}

	@Test
	fun `POST request is recognized correctly`() {
		val request = mock<Request>().apply { whenever(method).thenReturn(POST) }
		assertThat(request.isGET, equalTo(false))
		assertThat(request.isPOST, equalTo(true))
	}

}
