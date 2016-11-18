package net.pterodactylus.sone.freenet

/**
 * Container for an l10n key and optional values.
 */
data class L10nText(val text: String, val parameters: List<Any?> = emptyList())
