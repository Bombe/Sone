package net.pterodactylus.sone.web.ajax

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [JsonErrorReturnObject].
 */
class JsonErrorReturnObjectTest {

	private val returnObject = JsonErrorReturnObject("nope")

	@Test
	fun `error return object is not successful`() {
		assertThat(returnObject.isSuccess, equalTo(false))
	}

	@Test
	fun `error return object exposes error`() {
		assertThat(returnObject.error, equalTo("nope"))
	}

}
