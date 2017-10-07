package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Album.Modifier.AlbumTitleMustNotBeEmpty
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user edit the name and description of an album.
 */
class EditAlbumPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("editAlbum.html", template, "Page.EditAlbum.Title", webInterface, true) {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			val album = webInterface.core.getAlbum(freenetRequest.httpRequest.getPartAsStringFailsafe("album", 36)) ?: throw RedirectException("invalid.html")
			album.takeUnless { it.sone.isLocal }?.run { throw RedirectException("noPermission.html") }
			if (freenetRequest.httpRequest.getPartAsStringFailsafe("moveLeft", 4) == "true") {
				album.parent?.moveAlbumUp(album)
				webInterface.core.touchConfiguration()
				throw RedirectException("imageBrowser.html?album=${album.parent?.id}")
			} else if (freenetRequest.httpRequest.getPartAsStringFailsafe("moveRight", 4) == "true") {
				album.parent?.moveAlbumDown(album)
				webInterface.core.touchConfiguration()
				throw RedirectException("imageBrowser.html?album=${album.parent?.id}")
			} else {
				try {
					album.modify()
							.setTitle(freenetRequest.httpRequest.getPartAsStringFailsafe("title", 100))
							.setDescription(freenetRequest.httpRequest.getPartAsStringFailsafe("description", 1000))
							.update()
				} catch (e: AlbumTitleMustNotBeEmpty) {
					throw RedirectException("emptyAlbumTitle.html")
				}
				webInterface.core.touchConfiguration()
				throw RedirectException("imageBrowser.html?album=${album.id}")
			}
		}
	}

}
