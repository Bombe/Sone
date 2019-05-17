package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * This page lets the user lock a [net.pterodactylus.sone.data.Sone] to prevent it from being inserted.
 */
class LockSonePage @Inject constructor(template: Template, webInterface: WebInterface, loaders: Loaders):
		SoneTemplatePage("lockSone.html", webInterface, loaders, template = template, pageTitleKey = "Page.LockSone.Title") {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		soneRequest.parameters["returnPage", 256]!!.let { returnPage ->
			soneRequest.parameters["sone", 44]!!
					.let { soneRequest.core.getLocalSone(it) }
					?.let { soneRequest.core.lockSone(it) }
			throw RedirectException(returnPage)
		}
	}

}
