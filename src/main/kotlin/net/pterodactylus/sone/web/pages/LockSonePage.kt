package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * This page lets the user lock a [net.pterodactylus.sone.data.Sone] to prevent it from being inserted.
 */
@ToadletPath("lockSone.html")
class LockSonePage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		SoneTemplatePage(webInterface, loaders, templateRenderer, pageTitleKey = "Page.LockSone.Title") {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		soneRequest.parameters["returnPage", 256]!!.let { returnPage ->
			soneRequest.parameters["sone", 44]!!
					.let { soneRequest.core.getLocalSone(it) }
					?.let { soneRequest.core.lockSone(it) }
			redirectTo(returnPage)
		}
	}

}
