package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Album.Modifier.AlbumTitleMustNotBeEmpty
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Page that lets the user create a new album.
 */
class CreateAlbumPage @Inject constructor(template: Template, webInterface: WebInterface):
		LoggedInPage("createAlbum.html", template, "Page.CreateAlbum.Title", webInterface) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			val name = soneRequest.httpRequest.getPartAsStringFailsafe("name", 64).trim()
			if (name.isEmpty()) {
				templateContext["nameMissing"] = true
				return
			}
			val description = soneRequest.httpRequest.getPartAsStringFailsafe("description", 256).trim()
			val parentId = soneRequest.httpRequest.getPartAsStringFailsafe("parent", 36)
			val parent = if (parentId == "") currentSone.rootAlbum else soneRequest.core.getAlbum(parentId)
			val album = soneRequest.core.createAlbum(currentSone, parent)
			try {
				album.modify().apply {
					setTitle(name)
					setDescription(TextFilter.filter(soneRequest.httpRequest.getHeader("Host"), description))
				}.update()
			} catch (e: AlbumTitleMustNotBeEmpty) {
				throw RedirectException("emptyAlbumTitle.html")
			}
			soneRequest.core.touchConfiguration()
			throw RedirectException("imageBrowser.html?album=${album.id}")
		}
	}

}
