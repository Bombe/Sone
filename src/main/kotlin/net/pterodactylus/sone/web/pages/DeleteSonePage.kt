package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Lets the user delete a Sone. Of course the Sone is not really deleted from
 * Freenet; merely all references to it are removed from the local plugin
 * installation.
 */
@MenuName("DeleteSone")
class DeleteSonePage @Inject constructor(template: Template, webInterface: WebInterface):
		LoggedInPage("deleteSone.html", template, "Page.DeleteSone.Title", webInterface) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			if (soneRequest.httpRequest.isPartSet("deleteSone")) {
				soneRequest.core.deleteSone(currentSone)
			}
			throw RedirectException("index.html")
		}
	}

}
