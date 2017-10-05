package net.pterodactylus.sone.web.ajax

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import net.pterodactylus.sone.core.ElementLoader
import net.pterodactylus.sone.core.LinkedElement
import net.pterodactylus.sone.template.LinkedElementRenderFilter
import net.pterodactylus.sone.utils.jsonArray
import net.pterodactylus.sone.utils.jsonObject
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest

/**
 * Renders linked elements after they have been loaded.
 */
class GetLinkedElementAjaxPage(webInterface: WebInterface, private val elementLoader: ElementLoader, private val linkedElementRenderFilter: LinkedElementRenderFilter):
		JsonPage("getLinkedElement.ajax", webInterface) {

	override val needsFormPassword = false

	override fun createJsonObject(request: FreenetRequest): JsonReturnObject =
			request.httpRequest.getParam("elements", "[]").asJson()
					.map(JsonNode::asText)
					.map(elementLoader::loadElement)
					.filterNot { it.loading }
					.map { it to renderLinkedElement(it) }
					.let { elements ->
						jsonArray(
								*(elements.map { element ->
									jsonObject {
										put("link", element.first.link)
										put("html", element.second)
									}
								}.toTypedArray())
						)
					}.let { linkedElements ->
				createSuccessJsonObject().apply {
					put("linkedElements", linkedElements)
				}
			}

	override fun requiresLogin() = false

	private fun String.asJson() = ObjectMapper().readTree(this).asIterable()

	private fun renderLinkedElement(linkedElement: LinkedElement) =
			linkedElementRenderFilter.format(null, linkedElement, emptyMap())

}
