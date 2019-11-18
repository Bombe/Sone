package net.pterodactylus.sone.freenet

import freenet.l10n.*
import java.util.*

/**
 * [Translation] implementation based on Fred’s [BaseL10n].
 */
class BaseL10nTranslation(private val baseL10n: BaseL10n) : Translation {

	override val currentLocale: Locale
		get() = Locale(baseL10n.selectedLanguage.shortCode)

	override fun translate(key: String): String = baseL10n.getString(key)

}
