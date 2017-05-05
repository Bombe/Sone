package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.Pagination
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * This page shows all known Sones.
 */
class KnownSonesPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("knownSones.html", template, "Page.KnownSones.Title", webInterface, false) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		getCurrentSone(request.toadletContext).let { currentSone ->
			Pagination(webInterface.core.sones
					.filterNot { request.parameters["filter"] == "followed" && currentSone != null && !currentSone.hasFriend(it.id) }
					.filterNot { request.parameters["filter"] == "not-followed" && currentSone != null && currentSone.hasFriend(it.id) }
					.filterNot { request.parameters["filter"] == "new" && it.isKnown }
					.filterNot { request.parameters["filter"] == "not-new" && !it.isKnown }
					.filterNot { request.parameters["filter"] == "own" && !it.isLocal }
					.filterNot { request.parameters["filter"] == "not-own" && it.isLocal }
					.sortedWith(
							when (request.parameters["sort"]) {
								"images" -> Sone.IMAGE_COUNT_COMPARATOR
								"name" -> Sone.NICE_NAME_COMPARATOR.reversed()
								"posts" -> Sone.POST_COUNT_COMPARATOR
								else -> Sone.LAST_ACTIVITY_COMPARATOR
							}.let { comparator ->
								when (request.parameters["order"]) {
									"asc" -> comparator.reversed()
									else -> comparator
								}
							}
					), 25).apply { page = request.parameters["page"]?.toIntOrNull() ?: 0 }
					.let { pagination ->
						templateContext["pagination"] = pagination
						templateContext["knownSones"] = pagination.items
					}
			templateContext["sort"] = request.parameters["sort"].let { sort -> if (sort in listOf("images", "name", "posts")) sort else "activity" }
			templateContext["order"] = request.parameters["order"].let { order -> if (order == "asc") "asc" else "desc" }
			templateContext["filter"] = request.parameters["filter"]
		}
	}

}
