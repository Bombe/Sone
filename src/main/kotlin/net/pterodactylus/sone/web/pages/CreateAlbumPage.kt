package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.data.Album.Modifier.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.text.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Page that lets the user create a new album.
 */
@TemplatePath("/templates/createAlbum.html")
@ToadletPath("createAlbum.html")
class CreateAlbumPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("Page.CreateAlbum.Title", webInterface, loaders, templateRenderer) {

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
