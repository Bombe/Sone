package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.template.ParserFilter
import net.pterodactylus.sone.template.RenderFilter
import net.pterodactylus.sone.template.ShortenFilter
import net.pterodactylus.sone.text.TextFilter
import net.pterodactylus.sone.utils.headers
import net.pterodactylus.sone.utils.ifTrue
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that stores a user’s image modifications.
 */
class EditImageAjaxPage(webInterface: WebInterface,
		private val parserFilter: ParserFilter,
		private val shortenFilter: ShortenFilter,
		private val renderFilter: RenderFilter) : JsonPage("editImage.ajax", webInterface) {

	override fun createJsonObject(request: FreenetRequest) =
			request.parameters["image"]
					.let(core::getImage)
					?.let { image ->
						image.sone.isLocal.ifTrue {
							when {
								request.parameters["moveLeft"] == "true" -> createSuccessJsonObject().apply {
									put("sourceImageId", image.id)
									put("destinationImageId", image.album.moveImageUp(image).id)
									core.touchConfiguration()
								}
								request.parameters["moveRight"] == "true" -> createSuccessJsonObject().apply {
									put("sourceImageId", image.id)
									put("destinationImageId", image.album.moveImageDown(image).id)
									core.touchConfiguration()
								}
								else -> request.parameters["title"]!!.let { title ->
									title.trim().isNotBlank().ifTrue {
										request.parameters["description"]!!.let { description ->
											image.modify()
													.setTitle(title)
													.setDescription(TextFilter.filter(request.headers["Host"], description))
													.update().let { newImage ->
												createSuccessJsonObject().apply {
													put("title", newImage.title)
													put("description", newImage.description)
													put("parsedDescription", newImage.description.let {
														parserFilter.format(TemplateContext(), it, mutableMapOf("sone" to image.sone)).let {
															shortenFilter.format(TemplateContext(), it, mutableMapOf()).let {
																renderFilter.format(TemplateContext(), it, mutableMapOf()) as String
															}
														}
													})
													core.touchConfiguration()
												}
											}
										}
									} ?: createErrorJsonObject("invalid-image-title")
								}
							}
						} ?: createErrorJsonObject("not-authorized")
					} ?: createErrorJsonObject("invalid-image-id")

}
