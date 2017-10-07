package net.pterodactylus.sone.utils

import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import net.pterodactylus.util.template.TemplateParser
import java.io.StringReader
import java.io.StringWriter

fun String.asTemplate(): Template = StringReader(this).use { TemplateParser.parse(it) }

fun Template.render(templateContext: TemplateContext) =
		StringWriter().use {
			it.also {
				render(templateContext, it)
			}
		}.toString()
