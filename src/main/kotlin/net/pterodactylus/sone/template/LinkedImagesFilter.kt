package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.ImageLoader
import net.pterodactylus.sone.core.LoadedImage
import net.pterodactylus.sone.text.FreenetLinkPart
import net.pterodactylus.sone.text.Part
import net.pterodactylus.util.template.Filter
import net.pterodactylus.util.template.TemplateContext

/**
 * Filter that takes a number of pre-rendered [Part]s and replaces all identified links to freenet images
 * with [LoadedImage]s.
 */
class LinkedImagesFilter(private val imageLoader: ImageLoader) : Filter {

	@Suppress("UNCHECKED_CAST")
	override fun format(templateContext: TemplateContext?, data: Any?, parameters: MutableMap<String, Any?>?) =
			(data as? Iterable<Part>)
					?.filterIsInstance<FreenetLinkPart>()
					?.mapNotNull { imageLoader.toLoadedImage(it.link) }
					?: listOf<LoadedImage>()

}
