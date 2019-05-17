package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Page that lets the user delete an {@link Image}.
 */
@TemplatePath("/templates/deleteImage.html")
class DeleteImagePage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer):
		LoggedInPage("deleteImage.html", "Page.DeleteImage.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			val image = soneRequest.core.getImage(soneRequest.httpRequest.getPartAsStringFailsafe("image", 36)) ?: throw RedirectException("invalid.html")
			if (!image.sone.isLocal) {
				throw RedirectException("noPermission.html")
			}
			if (soneRequest.httpRequest.isPartSet("abortDelete")) {
				throw RedirectException("imageBrowser.html?image=${image.id}")
			}
			soneRequest.core.deleteImage(image)
			throw RedirectException("imageBrowser.html?album=${image.album.id}")
		}
		val image = soneRequest.core.getImage(soneRequest.httpRequest.getParam("image")) ?: throw RedirectException("invalid.html")
		if (!image.sone.isLocal) {
			throw RedirectException("noPermission.html")
		}
		templateContext["image"] = image
	}

}
