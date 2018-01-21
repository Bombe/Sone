package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.utils.mapPresent
import net.pterodactylus.sone.utils.paginate
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Page that displays all new posts and replies. The posts are filtered using
 * [PostVisibilityFilter.isPostVisible(Sone, Post)] and sorted by time.
 */
class NewPage @Inject constructor(  template: Template, webInterface: WebInterface):
		SoneTemplatePage("new.html", webInterface, template, "Page.New.Title") {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) =
			getCurrentSone(freenetRequest.toadletContext).let { currentSone ->
				(webInterface.getNewPosts(currentSone) + webInterface.getNewReplies(currentSone).mapPresent { it.post })
						.distinct()
						.sortedByDescending { it.time }
						.let { posts ->
							posts.paginate(webInterface.core.preferences.postsPerPage)
									.turnTo(freenetRequest.parameters["page"]?.toIntOrNull() ?: 0)
									.let { pagination ->
										templateContext["pagination"] = pagination
										templateContext["posts"] = pagination.items
									}
						}
			}

}
