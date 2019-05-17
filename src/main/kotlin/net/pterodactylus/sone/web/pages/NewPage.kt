package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.mapPresent
import net.pterodactylus.sone.utils.paginate
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Page that displays all new posts and replies. The posts are filtered using
 * [PostVisibilityFilter.isPostVisible(Sone, Post)] and sorted by time.
 */
@MenuName("New")
@TemplatePath("/templates/new.html")
class NewPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer):
		SoneTemplatePage("new.html", webInterface, loaders, templateRenderer, pageTitleKey = "Page.New.Title") {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) =
			getCurrentSone(soneRequest.toadletContext).let { currentSone ->
				(soneRequest.webInterface.getNewPosts(currentSone) + soneRequest.webInterface.getNewReplies(currentSone).mapPresent { it.post })
						.distinct()
						.sortedByDescending { it.time }
						.let { posts ->
							posts.paginate(soneRequest.core.preferences.postsPerPage)
									.turnTo(soneRequest.parameters["page"]?.toIntOrNull() ?: 0)
									.let { pagination ->
										templateContext["pagination"] = pagination
										templateContext["posts"] = pagination.items
									}
						}
			}

}
