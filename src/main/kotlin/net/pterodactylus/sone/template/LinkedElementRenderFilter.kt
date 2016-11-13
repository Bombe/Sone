package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.LinkedElement
import net.pterodactylus.sone.core.LinkedImage
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
		private val loadedImageTemplate = """<a href="/<% link|html>"><span class="linked-element" title="<% link|html>" style="background-image: url('/<% link|html>')"></span></a>""".parse()
		private val notLoadedImageTemplate = """<span class="linked-element" title="<% link|html>" style="background-image: url('images/loading-animation.gif')"></span>""".parse()

		private fun String.parse() = StringReader(this).use { TemplateParser.parse(it) }
	}

	override fun format(templateContext: TemplateContext?, data: Any?, parameters: Map<String, Any?>?) =
			when {
				data is LinkedElement && data.loading -> renderNotLoadedLinkedElement(data)
				data is LinkedImage -> renderLinkedImage(data)
				else -> null
			}

	private fun renderLinkedImage(linkedImage: LinkedImage) =
			StringWriter().use {
				val templateContext = templateContextFactory.createTemplateContext()
				templateContext["link"] = linkedImage.link
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
