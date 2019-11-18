package net.pterodactylus.sone.freenet

import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import java.util.*
import kotlin.collections.*

/**
 * Unit test for [L10nFilter].
 */
class L10nFilterTest {

	private val translations = mutableMapOf<String, String>()
	private val translation = object : Translation {
		override val currentLocale = Locale.ENGLISH
		override fun translate(key: String): String = translations[key] ?: ""
	}
	private val filter = L10nFilter(translation)

	@Test
	fun `translation without parameters returns translated string`() {
		translations["data"] = "translated data"
		assertThat(filter.format(null, "data", emptyMap()), equalTo("translated data"))
	}

	@Test
	fun `translation with parameters returned translated string`() {
		translations["data"] = "translated {0,number} {1}"
		assertThat(filter.format(null, "data", mapOf("0" to 4.5, "1" to "data")), equalTo("translated 4.5 data"))
	}

	@Test
	fun `filter processes l10n text without parameters correctly`() {
		translations["data"] = "translated data"
		assertThat(filter.format(null, L10nText("data"), emptyMap()), equalTo("translated data"))
	}

	@Test
	fun `filter processes l10n text with parameters correctly`() {
		translations["data"] = "translated {0,number} {1}"
		assertThat(filter.format(null, L10nText("data", listOf(4.5, "data")), emptyMap()), equalTo("translated 4.5 data"))
	}

	@Test
	fun `filter does not replace values if there are no parameters`() {
		translations["data"] = "{link}"
		assertThat(filter.format(null, "data", emptyMap()), equalTo("{link}"))
	}

}
