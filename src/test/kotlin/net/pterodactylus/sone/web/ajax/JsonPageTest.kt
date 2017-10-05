package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.web.WebInterface
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Base class for tests for any [JsonPage] implementations.
 */
abstract class JsonPageTest(
		private val expectedPath: String,
		private val requiresLogin: Boolean = true,
		private val needsFormPassword: Boolean = true,
		pageSupplier: (WebInterface) -> JsonPage = { mock() }): TestObjects() {

	protected open val page: JsonPage by lazy { pageSupplier(webInterface) }
	protected val json by lazy {
		page.createJsonObject(freenetRequest)
	}

	protected val JsonReturnObject.error get() = if (this is JsonErrorReturnObject) this.error else null

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo(expectedPath))
	}

	@Test
	fun `page needs form password`() {
		assertThat(page.needsFormPassword, equalTo(needsFormPassword))
	}

	@Test
	fun `page requires login`() {
		assertThat(page.requiresLogin, equalTo(requiresLogin))
	}

}
