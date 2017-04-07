package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.LinkedElement
import net.pterodactylus.util.template.Filter
import net.pterodactylus.util.template.TemplateContext
import net.pterodactylus.util.template.TemplateContextFactory
import net.pterodactylus.util.template.TemplateParser
import java.io.StringReader
import java.io.StringWriter

/**
 * Renders all kinds of [LinkedElement]s.
 */
class LinkedElementRenderFilter(private val templateContextFactory: TemplateContextFactory) : Filter {

	companion object {
		private val loadedImageTemplate = """<%include linked/image.html>""".parse()
		private val notLoadedImageTemplate = """<%include linked/notLoaded.html>""".parse()

		private fun String.parse() = StringReader(this).use { TemplateParser.parse(it) }!!
	}

	override fun format(templateContext: TemplateContext?, data: Any?, parameters: Map<String, Any?>?) =
			when {
				data is LinkedElement && data.loading -> renderNotLoadedLinkedElement(data)
				data is LinkedElement -> renderLinkedImage(data)
				else -> null
			}

	private fun renderLinkedImage(linkedElement: LinkedElement) =
			StringWriter().use {
				val templateContext = templateContextFactory.createTemplateContext()
				templateContext["link"] = linkedElement.link
				loadedImageTemplate.render(templateContext, it)
				it
			}.toString()

	private fun renderNotLoadedLinkedElement(linkedElement: LinkedElement) =
			StringWriter().use {
				val templateContext = templateContextFactory.createTemplateContext()
				templateContext["link"] = linkedElement.link
				notLoadedImageTemplate.render(templateContext, it)
				it
			}.toString()

}
