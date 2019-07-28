package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

@ToadletPath("debug")
class DebugPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		SoneTemplatePage(webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		soneRequest.core.setDebug()
		throw RedirectException("./")
	}

}
