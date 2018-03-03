package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.text.FreemailPart
import net.pterodactylus.sone.text.FreenetLinkPart
import net.pterodactylus.sone.text.LinkPart
import net.pterodactylus.sone.text.Part
import net.pterodactylus.sone.text.PlainTextPart
import net.pterodactylus.sone.text.PostPart
import net.pterodactylus.sone.text.SonePart
import net.pterodactylus.sone.text.SoneTextParser
import net.pterodactylus.sone.text.SoneTextParserContext
import net.pterodactylus.sone.utils.asTemplate
import net.pterodactylus.util.template.Filter
import net.pterodactylus.util.template.TemplateContext
import net.pterodactylus.util.template.TemplateContextFactory
import java.io.StringWriter
import java.io.Writer
import java.net.URLEncoder

/**
 * Renders a number of pre-parsed [Part] into a [String].
 */
class RenderFilter(private val core: Core, private val templateContextFactory: TemplateContextFactory) : Filter {

	companion object {
		private val plainTextTemplate = "<%text|html>".asTemplate()
		private val linkTemplate = "<a class=\"<%cssClass|html>\" href=\"<%link|html>\" title=\"<%title|html>\"><%text|html></a>".asTemplate()
	}

	override fun format(templateContext: TemplateContext?, data: Any?, parameters: MutableMap<String, Any?>?): Any? {
		@Suppress("UNCHECKED_CAST")
		val parts = data as? Iterable<Part> ?: return null
		val parsedTextWriter = StringWriter()
		render(parsedTextWriter, parts)
		return parsedTextWriter.toString()
	}

	private fun render(writer: Writer, parts: Iterable<Part>) {
		parts.forEach { render(writer, it) }
	}

	private fun render(writer: Writer, part: Part) {
		@Suppress("UNCHECKED_CAST")
		when (part) {
			is PlainTextPart -> render(writer, part)
			is FreenetLinkPart -> render(writer, part)
			is LinkPart -> render(writer, part)
			is SonePart -> render(writer, part)
			is PostPart -> render(writer, part)
			is FreemailPart -> render(writer, part)
		}
	}

	private fun render(writer: Writer, plainTextPart: PlainTextPart) {
		val templateContext = templateContextFactory.createTemplateContext()
		templateContext.set("text", plainTextPart.text)
		plainTextTemplate.render(templateContext, writer)
	}

	private fun render(writer: Writer, freenetLinkPart: FreenetLinkPart) {
		renderLink(writer, "/${freenetLinkPart.link}", freenetLinkPart.text, freenetLinkPart.title, if (freenetLinkPart.trusted) "freenet-trusted" else "freenet")
	}

	private fun render(writer: Writer, linkPart: LinkPart) {
		renderLink(writer, "/external-link/?_CHECKED_HTTP_=${linkPart.link.urlEncode()}", linkPart.text, linkPart.title, "internet")
	}

	private fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")

	private fun render(writer: Writer, sonePart: SonePart) {
		if (sonePart.sone.name != null) {
			renderLink(writer, "viewSone.html?sone=${sonePart.sone.id}", SoneAccessor.getNiceName(sonePart.sone), SoneAccessor.getNiceName(sonePart.sone), "in-sone")
		} else {
			renderLink(writer, "/WebOfTrust/ShowIdentity?id=${sonePart.sone.id}", sonePart.sone.id, sonePart.sone.id, "in-sone")
		}
	}

	private fun render(writer: Writer, postPart: PostPart) {
		val parser = SoneTextParser(core, core)
		val parserContext = SoneTextParserContext(postPart.post.sone)
		val parts = parser.parse(postPart.post.text, parserContext)
		val excerpt = StringBuilder()
		for (part in parts) {
			excerpt.append(part.text)
			if (excerpt.length > 20) {
				val lastSpace = excerpt.lastIndexOf(" ", 20)
				if (lastSpace > -1) {
					excerpt.setLength(lastSpace)
				} else {
					excerpt.setLength(20)
				}
				excerpt.append("…")
				break
			}
		}
		renderLink(writer, "viewPost.html?post=${postPart.post.id}", excerpt.toString(), SoneAccessor.getNiceName(postPart.post.sone), "in-sone")
	}

	private fun render(writer: Writer, freemailPart: FreemailPart) {
		val sone = core.getSone(freemailPart.identityId)
		val soneName = sone?.let(SoneAccessor::getNiceName) ?: freemailPart.identityId
		renderLink(writer,
				"/Freemail/NewMessage?to=${freemailPart.identityId}",
				"${freemailPart.emailLocalPart}@$soneName.freemail",
				"$soneName\n${freemailPart.emailLocalPart}@${freemailPart.freemailId}.freemail",
				"in-sone")
	}

	private fun renderLink(writer: Writer, link: String, text: String, title: String, cssClass: String) {
		val templateContext = templateContextFactory.createTemplateContext()
		templateContext["cssClass"] = cssClass
		templateContext["link"] = link
		templateContext["text"] = text
		templateContext["title"] = title
		linkTemplate.render(templateContext, writer)
	}

}
