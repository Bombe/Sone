package net.pterodactylus.sone.test

import net.pterodactylus.sone.main.*
import net.pterodactylus.util.template.*
import net.pterodactylus.util.web.*

/**
 * [Loaders] implementation for use in tests. Use [templates] to control what templates are
 * returned by the [loadTemplate] method.
 */
class TestLoaders : Loaders {

	val templates = mutableMapOf<String, Template>()

	override fun loadTemplate(path: String) = templates[path] ?: Template()

	override fun <REQ : Request> loadStaticPage(basePath: String, prefix: String, mimeType: String) = TestPage<REQ>()

	override fun getTemplateProvider() = TemplateProvider { _, _ -> Template() }

}
