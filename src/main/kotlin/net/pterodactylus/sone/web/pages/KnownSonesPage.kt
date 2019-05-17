package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.paginate
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * This page shows all known Sones.
 */
@MenuName("KnownSones")
class KnownSonesPage @Inject constructor(template: Template, webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer):
		SoneTemplatePage("knownSones.html", webInterface, loaders, template, templateRenderer, pageTitleKey = "Page.KnownSones.Title") {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		getCurrentSone(soneRequest.toadletContext).let { currentSone ->
			soneRequest.core.sones.asSequence()
					.filterNot { soneRequest.parameters["filter"] == "followed" && currentSone != null && !currentSone.hasFriend(it.id) }
					.filterNot { soneRequest.parameters["filter"] == "not-followed" && currentSone != null && currentSone.hasFriend(it.id) }
					.filterNot { soneRequest.parameters["filter"] == "new" && it.isKnown }
					.filterNot { soneRequest.parameters["filter"] == "not-new" && !it.isKnown }
					.filterNot { soneRequest.parameters["filter"] == "own" && !it.isLocal }
					.filterNot { soneRequest.parameters["filter"] == "not-own" && it.isLocal }
					.sortedWith(
							when (soneRequest.parameters["sort"]) {
								"images" -> Sone.IMAGE_COUNT_COMPARATOR
								"name" -> Sone.NICE_NAME_COMPARATOR.reversed()
								"posts" -> Sone.POST_COUNT_COMPARATOR
								else -> Sone.LAST_ACTIVITY_COMPARATOR
							}.let { comparator ->
								when (soneRequest.parameters["order"]) {
									"asc" -> comparator.reversed()
									else -> comparator
								}
							}
					).paginate(25)
					.turnTo(soneRequest.parameters["page"]?.toIntOrNull() ?: 0)
					.let { pagination ->
						templateContext["pagination"] = pagination
						templateContext["knownSones"] = pagination.items
					}
			templateContext["sort"] = soneRequest.parameters["sort"].let { sort -> if (sort in listOf("images", "name", "posts")) sort else "activity" }
			templateContext["order"] = soneRequest.parameters["order"].let { order -> if (order == "asc") "asc" else "desc" }
			templateContext["filter"] = soneRequest.parameters["filter"]
		}
	}

}
