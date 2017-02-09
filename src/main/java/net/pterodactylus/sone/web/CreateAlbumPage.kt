package net.pterodactylus.sone.web

import net.pterodactylus.sone.data.Album.Modifier.AlbumTitleMustNotBeEmpty
import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import net.pterodactylus.util.web.Method.POST

/**
 * Page that lets the user create a new album.
 */
class CreateAlbumPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("createAlbum.html", template, "Page.CreateAlbum.Title", webInterface, true) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		if (request.method == POST) {
			val name = request.httpRequest.getPartAsStringFailsafe("name", 64).trim()
			if (name.isEmpty()) {
				templateContext["nameMissing"] = true
				return
			}
			val description = request.httpRequest.getPartAsStringFailsafe("description", 256).trim()
			val currentSone = webInterface.getCurrentSoneCreatingSession(request.toadletContext)
			val parentId = request.httpRequest.getPartAsStringFailsafe("parent", 36)
			val parent = if (parentId == "") currentSone.rootAlbum else webInterface.core.getAlbum(parentId)
			val album = webInterface.core.createAlbum(currentSone, parent)
			try {
				album.modify().apply {
					setTitle(name)
					setDescription(TextFilter.filter(request.httpRequest.getHeader("Host"), description))
				}.update()
			} catch (e: AlbumTitleMustNotBeEmpty) {
				throw RedirectException("emptyAlbumTitle.html")
			}
			webInterface.core.touchConfiguration()
			throw RedirectException("imageBrowser.html?album=${album.id}")
		}
	}

}
