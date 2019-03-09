package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.util.template.*
import java.io.*

/**
 * Renders all kinds of [LinkedElement]s.
 */
class LinkedElementRenderFilter : Filter {

	private val templateContextFactory = TemplateContextFactory().apply {
		addFilter("html", HtmlFilter())
		addProvider(ClassPathTemplateProvider(LinkedElementRenderFilter::class.java, "/templates/"))
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

private val loadedImageTemplate = """<%include linked/image.html>""".asTemplate()
private val loadedHtmlPageTemplate = """<%include linked/html-page.html>""".asTemplate()
private val notLoadedImageTemplate = """<%include linked/notLoaded.html>""".asTemplate()
