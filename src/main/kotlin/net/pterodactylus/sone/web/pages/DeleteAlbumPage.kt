package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Page that lets the user delete an {@link Album}.
 */
@TemplatePath("/templates/deleteAlbum.html")
@ToadletPath("deleteAlbum.html")
class DeleteAlbumPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("Page.DeleteAlbum.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			val album = soneRequest.core.getAlbum(soneRequest.httpRequest.getPartAsStringFailsafe("album", 36)) ?: throw RedirectException("invalid.html")
			if (!album.sone.isLocal) {
				throw RedirectException("noPermission.html")
			}
			if (soneRequest.httpRequest.getPartAsStringFailsafe("abortDelete", 4) == "true") {
				throw RedirectException("imageBrowser.html?album=${album.id}")
			}
			soneRequest.core.deleteAlbum(album)
			throw RedirectException(if (album.parent.isRoot) "imageBrowser.html?sone=${album.sone.id}" else "imageBrowser.html?album=${album.parent.id}")
		}
		val album = soneRequest.core.getAlbum(soneRequest.httpRequest.getParam("album"))
		templateContext["album"] = album ?: throw RedirectException("invalid.html")
	}

}
