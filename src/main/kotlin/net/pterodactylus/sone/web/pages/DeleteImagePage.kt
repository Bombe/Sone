package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Page that lets the user delete an {@link Image}.
 */
class DeleteImagePage @Inject constructor(template: Template, webInterface: WebInterface):
		LoggedInPage("deleteImage.html", template, "Page.DeleteImage.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			val image = webInterface.core.getImage(freenetRequest.httpRequest.getPartAsStringFailsafe("image", 36)) ?: throw RedirectException("invalid.html")
			if (!image.sone.isLocal) {
				throw RedirectException("noPermission.html")
			}
			if (freenetRequest.httpRequest.isPartSet("abortDelete")) {
				throw RedirectException("imageBrowser.html?image=${image.id}")
			}
			webInterface.core.deleteImage(image)
			throw RedirectException("imageBrowser.html?album=${image.album.id}")
		}
		val image = webInterface.core.getImage(freenetRequest.httpRequest.getParam("image")) ?: throw RedirectException("invalid.html")
		if (!image.sone.isLocal) {
			throw RedirectException("noPermission.html")
		}
		templateContext["image"] = image
	}

}
