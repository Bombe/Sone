package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user delete an {@link Image}.
 */
class DeleteImagePage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("deleteImage.html", template, "Page.DeleteImage.Title", webInterface, true) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		if (request.isPOST) {
			val image = webInterface.core.getImage(request.httpRequest.getPartAsStringFailsafe("image", 36)) ?: throw RedirectException("invalid.html")
			if (!image.sone.isLocal) {
				throw RedirectException("noPermission.html")
			}
			if (request.httpRequest.isPartSet("abortDelete")) {
				throw RedirectException("imageBrowser.html?image=${image.id}")
			}
			webInterface.core.deleteImage(image)
			throw RedirectException("imageBrowser.html?album=${image.album.id}")
		}
		val image = webInterface.core.getImage(request.httpRequest.getParam("image")) ?: throw RedirectException("invalid.html")
		if (!image.sone.isLocal) {
			throw RedirectException("noPermission.html")
		}
		templateContext["image"] = image
	}

}
