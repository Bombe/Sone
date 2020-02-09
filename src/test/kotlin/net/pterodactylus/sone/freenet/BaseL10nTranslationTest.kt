package net.pterodactylus.sone.freenet

import freenet.l10n.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import java.util.*

/**
 * Test for [BaseL10nTranslation].
 */
class BaseL10nTranslationTest {

	private val baseL10n = mock<BaseL10n>()
	private val translation = BaseL10nTranslation(baseL10n)

	@Test
	fun `translate method is facade for the correct method`() {
		whenever(baseL10n.getString("test")).thenReturn("answer")
		assertThat(translation.translate("test"), equalTo("answer"))
	}

	@Test
	fun `language exposes correct short code`() {
		whenever(baseL10n.selectedLanguage).thenReturn(BaseL10n.LANGUAGE.ENGLISH)
		assertThat(translation.currentLocale, equalTo(Locale.ENGLISH))
	}

}
