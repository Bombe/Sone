package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.paginate
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * This page shows all known Sones.
 */
class KnownSonesPage @Inject constructor(template: Template, webInterface: WebInterface):
		SoneTemplatePage("knownSones.html", webInterface, template, "Page.KnownSones.Title") {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		getCurrentSone(freenetRequest.toadletContext).let { currentSone ->
			webInterface.core.sones
					.filterNot { freenetRequest.parameters["filter"] == "followed" && currentSone != null && !currentSone.hasFriend(it.id) }
					.filterNot { freenetRequest.parameters["filter"] == "not-followed" && currentSone != null && currentSone.hasFriend(it.id) }
					.filterNot { freenetRequest.parameters["filter"] == "new" && it.isKnown }
					.filterNot { freenetRequest.parameters["filter"] == "not-new" && !it.isKnown }
					.filterNot { freenetRequest.parameters["filter"] == "own" && !it.isLocal }
					.filterNot { freenetRequest.parameters["filter"] == "not-own" && it.isLocal }
					.sortedWith(
							when (freenetRequest.parameters["sort"]) {
								"images" -> Sone.IMAGE_COUNT_COMPARATOR
								"name" -> Sone.NICE_NAME_COMPARATOR.reversed()
								"posts" -> Sone.POST_COUNT_COMPARATOR
								else -> Sone.LAST_ACTIVITY_COMPARATOR
							}.let { comparator ->
								when (freenetRequest.parameters["order"]) {
									"asc" -> comparator.reversed()
									else -> comparator
								}
							}
					).paginate(25)
					.turnTo(freenetRequest.parameters["page"]?.toIntOrNull() ?: 0)
					.let { pagination ->
						templateContext["pagination"] = pagination
						templateContext["knownSones"] = pagination.items
					}
			templateContext["sort"] = freenetRequest.parameters["sort"].let { sort -> if (sort in listOf("images", "name", "posts")) sort else "activity" }
			templateContext["order"] = freenetRequest.parameters["order"].let { order -> if (order == "asc") "asc" else "desc" }
			templateContext["filter"] = freenetRequest.parameters["filter"]
		}
	}

}
