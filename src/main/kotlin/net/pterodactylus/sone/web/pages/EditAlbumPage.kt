package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.data.Album.Modifier.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Page that lets the user edit the name and description of an album.
 */
@ToadletPath("editAlbum.html")
class EditAlbumPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("Page.EditAlbum.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			val album = soneRequest.core.getAlbum(soneRequest.httpRequest.getPartAsStringFailsafe("album", 36)) ?: redirectTo("invalid.html")
			album.takeUnless { it.sone.isLocal }?.run { redirectTo("noPermission.html") }
			if (soneRequest.httpRequest.getPartAsStringFailsafe("moveLeft", 4) == "true") {
				album.parent?.moveAlbumUp(album)
				soneRequest.core.touchConfiguration()
				redirectTo("imageBrowser.html?album=${album.parent?.id}")
			} else if (soneRequest.httpRequest.getPartAsStringFailsafe("moveRight", 4) == "true") {
				album.parent?.moveAlbumDown(album)
				soneRequest.core.touchConfiguration()
				redirectTo("imageBrowser.html?album=${album.parent?.id}")
			} else {
				try {
					album.modify()
							.setTitle(soneRequest.httpRequest.getPartAsStringFailsafe("title", 100))
							.setDescription(soneRequest.httpRequest.getPartAsStringFailsafe("description", 1000))
							.update()
				} catch (e: AlbumTitleMustNotBeEmpty) {
					redirectTo("emptyAlbumTitle.html")
				}
				soneRequest.core.touchConfiguration()
				redirectTo("imageBrowser.html?album=${album.id}")
			}
		}
	}

}
