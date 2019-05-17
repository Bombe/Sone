package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Image.Modifier.ImageTitleMustNotBeEmpty
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Page that lets the user edit title and description of an {@link Image}.
 */
class EditImagePage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer):
		LoggedInPage("editImage.html", "Page.EditImage.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			val image = soneRequest.core.getImage(soneRequest.httpRequest.getPartAsStringFailsafe("image", 36)) ?: throw RedirectException("invalid.html")
			if (!image.sone.isLocal) {
				throw RedirectException("noPermission.html")
			}
			soneRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256).let { returnPage ->
				if (soneRequest.httpRequest.getPartAsStringFailsafe("moveLeft", 4) == "true") {
					image.album.moveImageUp(image)
					soneRequest.core.touchConfiguration()
				} else if (soneRequest.httpRequest.getPartAsStringFailsafe("moveRight", 4) == "true") {
					image.album.moveImageDown(image)
					soneRequest.core.touchConfiguration()
				} else {
					try {
						image.modify()
								.setTitle(soneRequest.httpRequest.getPartAsStringFailsafe("title", 100))
								.setDescription(TextFilter.filter(soneRequest.httpRequest.getHeader("Host"), soneRequest.httpRequest.getPartAsStringFailsafe("description", 1024)))
								.update()
						soneRequest.core.touchConfiguration()
					} catch (e: ImageTitleMustNotBeEmpty) {
						throw RedirectException("emptyImageTitle.html")
					}
				}
				throw RedirectException(returnPage)
			}
		}
	}

}
