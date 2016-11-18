package net.pterodactylus.sone.freenet

import freenet.l10n.BaseL10n
import freenet.l10n.BaseL10n.LANGUAGE.ENGLISH
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.util.template.TemplateContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString

/**
 * Unit test for [L10nFilter].
 */
class L10nFilterTest {

	private val webInterface = mock<WebInterface>()
	private val filter = L10nFilter(webInterface)
	private val templateContext = mock<TemplateContext>()
	private val l10n = mock<BaseL10n>()
	private val translations = mutableMapOf<String, String>()

	@Before
	fun setupWebInterface() {
		whenever(webInterface.l10n).thenReturn(l10n)
	}

	@Before
	fun setupL10n() {
		whenever(l10n.selectedLanguage).thenReturn(ENGLISH)
		whenever(l10n.getString(anyString())).then { translations[it.arguments[0]] }
	}

	@Test
	fun `translation without parameters returns translated string`() {
		translations["data"] = "translated data"
		assertThat(filter.format(templateContext, "data", emptyMap()), equalTo("translated data"))
	}

	@Test
	fun `translation with parameters returned translated string`() {
		translations["data"] = "translated {0,number} {1}"
		assertThat(filter.format(templateContext, "data", mapOf("0" to 4.5, "1" to "data")), equalTo("translated 4.5 data"))
	}

	@Test
	fun `filter processes l10n text without parameters correctly`() {
		translations["data"] = "translated data"
		assertThat(filter.format(templateContext, L10nText("data"), emptyMap()), equalTo("translated data"))
	}

	@Test
	fun `filter processes l10n text with parameters correctly`() {
		translations["data"] = "translated {0,number} {1}"
		assertThat(filter.format(templateContext, L10nText("data", listOf(4.5, "data")), emptyMap()), equalTo("translated 4.5 data"))
	}

	@Test
	fun `filter does not replace values if there are no parameters`() {
		translations["data"] = "{link}"
		assertThat(filter.format(templateContext, "data", emptyMap()), equalTo("{link}"));
	}

}
