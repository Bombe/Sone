package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.utils.headers
import net.pterodactylus.sone.utils.ifTrue
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * Page that stores a user’s album modifications.
 */
class EditAlbumAjaxPage(webInterface: WebInterface) : JsonPage("editAlbum.ajax", webInterface) {

	override fun createJsonObject(request: FreenetRequest) =
			request.parameters["album"]!!
					.let(core::getAlbum)
					?.let { album ->
						album.sone.isLocal.ifTrue {
							when {
								request.parameters["moveLeft"] == "true" -> createSuccessJsonObject().apply {
									put("sourceAlbumId", album.id)
									put("destinationAlbumId", album.parent.moveAlbumUp(album).id)
								}
								request.parameters["moveRight"] == "true" -> createSuccessJsonObject().apply {
									put("sourceAlbumId", album.id)
									put("destinationAlbumId", album.parent.moveAlbumDown(album).id)
								}
								else -> try {
									album.modify()
											.setTitle(request.parameters["title"])
											.setDescription(TextFilter.filter(request.headers["Host"], request.parameters["description"]))
											.update()
									createSuccessJsonObject()
											.put("albumId", album.id)
											.put("title", album.title)
											.put("description", album.description)
								} catch (e: IllegalStateException) {
									createErrorJsonObject("invalid-album-title")
								}
							}
						} ?: createErrorJsonObject("not-authorized")
					} ?: createErrorJsonObject("invalid-album-id")

}
