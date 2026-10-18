package net.pterodactylus.sone.web.pages

import jakarta.inject.Inject
import net.pterodactylus.sone.core.DefaultElementLoader
import net.pterodactylus.sone.main.Loaders
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.MenuName
import net.pterodactylus.sone.web.page.SoneRequest
import net.pterodactylus.sone.web.page.TemplatePath
import net.pterodactylus.sone.web.page.TemplateRenderer
import net.pterodactylus.sone.web.page.ToadletPath
import net.pterodactylus.util.template.TemplateContext

@MenuName("Elements")
@TemplatePath("/templates/elements.html")
@ToadletPath("elements.html")
class ElementsPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer, private val elementsLoader: DefaultElementLoader) :
	SoneTemplatePage(webInterface, loaders, templateRenderer, pageTitleKey = "Page.ElementLoader.Title") {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		templateContext.set("loadingElements", elementsLoader.loadingElements)
		templateContext.set("loadedElements", elementsLoader.loadedElements)
		templateContext.set("rejectedElements", elementsLoader.rejectedElements)
		templateContext.set("failedElements", elementsLoader.failedElements)
	}

}
