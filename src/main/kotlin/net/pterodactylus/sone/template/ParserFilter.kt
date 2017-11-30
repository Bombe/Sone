package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.text.Part
import net.pterodactylus.sone.text.SoneTextParser
import net.pterodactylus.sone.text.SoneTextParserContext
import net.pterodactylus.util.template.Filter
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses a [String] into a number of [Part]s.
 */
@Singleton
class ParserFilter @Inject constructor(private val core: Core, private val soneTextParser: SoneTextParser) : Filter {

	override fun format(templateContext: TemplateContext?, data: Any?, parameters: MutableMap<String, Any?>?): Any? {
		val text = data?.toString() ?: return listOf<Part>()
		val soneParameter = parameters?.get("sone")
		val sone = when (soneParameter) {
			is String -> core.getSone(soneParameter)
			is Sone -> soneParameter
			else -> null
		}
		val context = SoneTextParserContext(sone)
		return soneTextParser.parse(text, context)
	}

}
