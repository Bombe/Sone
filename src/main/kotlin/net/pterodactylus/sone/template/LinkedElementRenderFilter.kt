package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.LinkedElement
import net.pterodactylus.util.template.Filter
import net.pterodactylus.util.template.TemplateContext
import net.pterodactylus.util.template.TemplateContextFactory
import net.pterodactylus.util.template.TemplateParser
import java.io.StringReader
import java.io.StringWriter
import javax.inject.Inject

/**
 * Renders all kinds of [LinkedElement]s.
 */
class LinkedElementRenderFilter @Inject constructor(private val templateContextFactory: TemplateContextFactory): Filter {

	companion object {
		private val loadedImageTemplate = """<%include linked/image.html>""".parse()
		private val loadedHtmlPageTemplate = """<%include linked/html-page.html>""".parse()
		private val notLoadedImageTemplate = """<%include linked/notLoaded.html>""".parse()

		private fun String.parse() = StringReader(this).use { TemplateParser.parse(it) }!!
	}

	override fun format(templateContext: TemplateContext?, data: Any?, parameters: Map<String, Any?>?) =
			when {
				data is LinkedElement && data.loading -> renderNotLoadedLinkedElement(data)
				data is LinkedElement && data.properties["type"] == "image" -> renderLinkedImage(data)
				data is LinkedElement && data.properties["type"] == "html" -> renderHtmlPage(data)
				else -> null
			}

	private fun renderLinkedImage(linkedElement: LinkedElement) =
			StringWriter().use {
				val templateContext = templateContextFactory.createTemplateContext()
				templateContext["link"] = linkedElement.link
				it.also { loadedImageTemplate.render(templateContext, it) }
			}.toString()

	private fun renderHtmlPage(linkedElement: LinkedElement) =
			StringWriter().use {
				val templateContext = templateContextFactory.createTemplateContext()
				templateContext["link"] = linkedElement.link
				templateContext["title"] = linkedElement.properties["title"] ?: "No title"
				templateContext["description"] = linkedElement.properties["description"] ?: "No description"
				it.also { loadedHtmlPageTemplate.render(templateContext, it) }
			}.toString()

	private fun renderNotLoadedLinkedElement(linkedElement: LinkedElement) =
			StringWriter().use {
				val templateContext = templateContextFactory.createTemplateContext()
				templateContext["link"] = linkedElement.link
				it.also { notLoadedImageTemplate.render(templateContext, it) }
			}.toString()

}
