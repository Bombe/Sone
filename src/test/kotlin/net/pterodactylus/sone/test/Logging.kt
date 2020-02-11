package net.pterodactylus.sone.test

import org.junit.rules.TestRule
import org.junit.runners.model.Statement
import java.util.logging.Level
import java.util.logging.Logger.getLogger

/**
 * Silences the `net.pterodactylus.sone` [logger][java.util.logging.Logger] during a test.
 */
fun silencedLogging() = TestRule { base, _ ->
	object : Statement() {
		override fun evaluate() {
			getLogger("net.pterodactylus.sone").let { logger ->
				val oldLevel = logger.level
				logger.level = Level.OFF
				try {
					base.evaluate()
				} finally {
					logger.level = oldLevel
				}
			}
		}
	}
}
