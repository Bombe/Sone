package net.pterodactylus.sone.web

import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import net.pterodactylus.util.web.Method.POST

/**
 * Page that lets the user delete an {@link Album}.
 */
class DeleteAlbumPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("deleteAlbum.html", template, "Page.DeleteAlbum.Title", webInterface, true) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		val album = webInterface.core.getAlbum(request.httpRequest.getPartAsStringFailsafe("album", 36))
		templateContext["album"] = album ?: throw RedirectException("invalid.html")
		if (request.method == POST) {
			if (!album.sone.isLocal) {
				throw RedirectException("noPermission.html")
			}
			if (request.httpRequest.getPartAsStringFailsafe("abortDelete", 4) == "true") {
				throw RedirectException("imageBrowser.html?album=${album.id}")
			}
			webInterface.core.deleteAlbum(album)
			throw RedirectException(if (album.parent.isRoot) "imageBrowser.html?sone=${album.sone.id}" else "imageBrowser.html?album=${album.parent.id}")
		}
	}

}
