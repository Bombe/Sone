package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.utils.Pagination
import net.pterodactylus.sone.utils.mapPresent
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that displays all new posts and replies. The posts are filtered using
 * [PostVisibilityFilter.isPostVisible(Sone, Post)] and sorted by time.
 */
class NewPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("new.html", template, "Page.New.Title", webInterface, false) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) =
			getCurrentSone(request.toadletContext).let { currentSone ->
				(webInterface.getNewPosts(currentSone) + webInterface.getNewReplies(currentSone).mapPresent { it.post })
						.distinct()
						.sortedByDescending { it.time }
						.let { posts ->
							Pagination(posts, webInterface.core.preferences.postsPerPage).apply {
								page = request.parameters["page"]?.toIntOrNull() ?: 0
							}.let { pagination ->
								templateContext["pagination"] = pagination
								templateContext["posts"] = pagination.items
							}
						}
			}

}
