package net.pterodactylus.sone.template

import net.pterodactylus.sone.text.LinkPart
import net.pterodactylus.sone.text.Part
import net.pterodactylus.sone.text.PlainTextPart
import net.pterodactylus.util.template.Filter
import net.pterodactylus.util.template.TemplateContext
import java.util.*

/**
 * [Filter] that shortens a number of [Part]s in order to restrict the maximum visible text length.
 */
class ShortenFilter : Filter {

	override fun format(templateContext: TemplateContext?, data: Any?, parameters: Map<String, Any?>?): Any? {
		@Suppress("UNCHECKED_CAST")
		val parts = data as? Iterable<Part> ?: return null
		val length = parameters?.parseInt("length") ?: -1
		val cutOffLength = parameters?.parseInt("cut-off-length") ?: length
		if (length > -1) {
			var allPartsLength = 0
			val shortenedParts = ArrayList<Part>()
			for (part in parts) {
				if (part is PlainTextPart) {
					val longText = part.text
					if (allPartsLength < cutOffLength) {
						if (allPartsLength + longText.length > cutOffLength) {
							shortenedParts.add(PlainTextPart(longText.substring(0, cutOffLength - allPartsLength) + "…"))
						} else {
							shortenedParts.add(part)
						}
					}
					allPartsLength += longText.length
				} else if (part is LinkPart) {
					if (allPartsLength < cutOffLength) {
						shortenedParts.add(part)
					}
					allPartsLength += part.text.length
				} else {
					if (allPartsLength < cutOffLength) {
						shortenedParts.add(part)
					}
				}
			}
			if (allPartsLength > length) {
				return shortenedParts
			}
		}
		return parts
	}

	private fun Map<String, Any?>.parseInt(key: String) = this[key]?.toString()?.toInt()

}
