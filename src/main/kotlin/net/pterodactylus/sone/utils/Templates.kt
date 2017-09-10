package net.pterodactylus.sone.utils

import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateParser
import java.io.StringReader

fun String.asTemplate(): Template = StringReader(this).use { TemplateParser.parse(it) }
