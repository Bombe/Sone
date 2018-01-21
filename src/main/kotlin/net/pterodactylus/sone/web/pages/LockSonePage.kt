package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * This page lets the user lock a [net.pterodactylus.sone.data.Sone] to prevent it from being inserted.
 */
class LockSonePage @Inject constructor(template: Template, webInterface: WebInterface):
		SoneTemplatePage("lockSone.html", webInterface, template, "Page.LockSone.Title") {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		freenetRequest.parameters["returnPage", 256]!!.let { returnPage ->
			freenetRequest.parameters["sone", 44]!!
					.let { webInterface.core.getLocalSone(it) }
					?.let { webInterface.core.lockSone(it) }
			throw RedirectException(returnPage)
		}
	}

}
