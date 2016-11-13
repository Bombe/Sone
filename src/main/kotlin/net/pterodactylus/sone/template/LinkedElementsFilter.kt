package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.ElementLoader
import net.pterodactylus.sone.core.LinkedElement
import net.pterodactylus.sone.text.FreenetLinkPart
import net.pterodactylus.sone.text.Part
import net.pterodactylus.util.template.Filter
import net.pterodactylus.util.template.TemplateContext

/**
 * Filter that takes a number of pre-rendered [Part]s and replaces all identified links to freenet elements
 * with [LinkedElement]s.
 */
class LinkedElementsFilter(private val elementLoader: ElementLoader) : Filter {

	@Suppress("UNCHECKED_CAST")
	override fun format(templateContext: TemplateContext?, data: Any?, parameters: MutableMap<String, Any?>?) =
			(data as? Iterable<Part>)
					?.filterIsInstance<FreenetLinkPart>()
					?.map { elementLoader.loadElement(it.link) }
					?: listOf<LinkedElement>()

}
