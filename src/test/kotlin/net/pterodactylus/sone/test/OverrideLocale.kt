package net.pterodactylus.sone.test

import org.junit.rules.ExternalResource
import java.util.Locale

/**
 * JUnit [test rule][org.junit.rules.TestRule] that overrides the
 * [JVM’s default locale][Locale.getDefault] for the duration of a test,
 * restoring it to its previous value after the test.
 */
class OverrideLocale(private val locale: Locale) : ExternalResource() {

	override fun before() {
		previousLocale = Locale.getDefault()
		Locale.setDefault(locale)
	}

	override fun after() {
		Locale.setDefault(previousLocale)
	}

	private var previousLocale: Locale? = null

}
