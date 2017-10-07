package net.pterodactylus.sone.text

/**
 * [Part] implementation that holds a single piece of text.
 *
 * @author [David Roden](mailto:d.roden@emetriq.com)
 */
data class PlainTextPart(override val text: String) : Part
