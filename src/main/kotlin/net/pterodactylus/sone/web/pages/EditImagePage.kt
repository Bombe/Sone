package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Image.Modifier.ImageTitleMustNotBeEmpty
import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user edit title and description of an {@link Image}.
 */
class EditImagePage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("editImage.html", template, "Page.EditImage.Title", webInterface, true) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		if (request.isPOST) {
			val image = webInterface.core.getImage(request.httpRequest.getPartAsStringFailsafe("image", 36)) ?: throw RedirectException("invalid.html")
			if (!image.sone.isLocal) {
				throw RedirectException("noPermission.html")
			}
			request.httpRequest.getPartAsStringFailsafe("returnPage", 256).let { returnPage ->
				if (request.httpRequest.getPartAsStringFailsafe("moveLeft", 4) == "true") {
					image.album.moveImageUp(image)
					webInterface.core.touchConfiguration()
				} else if (request.httpRequest.getPartAsStringFailsafe("moveRight", 4) == "true") {
					image.album.moveImageDown(image)
					webInterface.core.touchConfiguration()
				} else {
					try {
						image.modify()
								.setTitle(request.httpRequest.getPartAsStringFailsafe("title", 100))
								.setDescription(TextFilter.filter(request.httpRequest.getHeader("Host"), request.httpRequest.getPartAsStringFailsafe("description", 1024)))
								.update()
						webInterface.core.touchConfiguration()
					} catch (e: ImageTitleMustNotBeEmpty) {
						throw RedirectException("emptyImageTitle.html")
					}
				}
				throw RedirectException(returnPage)
			}
		}
	}

}
