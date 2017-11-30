package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Album.Modifier.AlbumTitleMustNotBeEmpty
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user create a new album.
 */
class CreateAlbumPage(template: Template, webInterface: WebInterface):
		LoggedInPage("createAlbum.html", template, "Page.CreateAlbum.Title", webInterface) {

	override fun handleRequest(freenetRequest: FreenetRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			val name = freenetRequest.httpRequest.getPartAsStringFailsafe("name", 64).trim()
			if (name.isEmpty()) {
				templateContext["nameMissing"] = true
				return
			}
			val description = freenetRequest.httpRequest.getPartAsStringFailsafe("description", 256).trim()
			val parentId = freenetRequest.httpRequest.getPartAsStringFailsafe("parent", 36)
			val parent = if (parentId == "") currentSone.rootAlbum else webInterface.core.getAlbum(parentId)
			val album = webInterface.core.createAlbum(currentSone, parent)
			try {
				album.modify().apply {
					setTitle(name)
					setDescription(TextFilter.filter(freenetRequest.httpRequest.getHeader("Host"), description))
				}.update()
			} catch (e: AlbumTitleMustNotBeEmpty) {
				throw RedirectException("emptyAlbumTitle.html")
			}
			webInterface.core.touchConfiguration()
			throw RedirectException("imageBrowser.html?album=${album.id}")
		}
	}

}
