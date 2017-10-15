package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.Pagination
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import java.net.URI

/**
 * The image browser page is the entry page for the image management.
 */
class ImageBrowserPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("imageBrowser.html", template, "Page.ImageBrowser.Title", webInterface, true) {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		if ("album" in freenetRequest.parameters) {
			templateContext["albumRequested"] = true
			templateContext["album"] = webInterface.core.getAlbum(freenetRequest.parameters["album"]!!)
			templateContext["page"] = freenetRequest.parameters["page"]
		} else if ("image" in freenetRequest.parameters) {
			templateContext["imageRequested"] = true
			templateContext["image"] = webInterface.core.getImage(freenetRequest.parameters["image"])
		} else if (freenetRequest.parameters["mode"] == "gallery") {
			templateContext["galleryRequested"] = true
			webInterface.core.sones
					.map(Sone::getRootAlbum)
					.flatMap(Album::getAlbums)
					.flatMap { Album.FLATTENER.apply(it)!! }
					.filterNot(Album::isEmpty)
					.sortedBy(Album::getTitle)
					.also { albums ->
						Pagination(albums, webInterface.core.preferences.imagesPerPage).apply { page = freenetRequest.parameters["page"]?.toIntOrNull() ?: 0 }.also { pagination ->
							templateContext["albumPagination"] = pagination
							templateContext["albums"] = pagination.items
						}
					}
		} else {
			templateContext["soneRequested"] = true
			templateContext["sone"] = webInterface.core.getSone(freenetRequest.httpRequest.getParam("sone")) ?: getCurrentSone(freenetRequest.toadletContext)
		}
	}

	override fun isLinkExcepted(link: URI?) = true

}
