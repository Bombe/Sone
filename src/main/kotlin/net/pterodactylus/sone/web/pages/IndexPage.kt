package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.notify.PostVisibilityFilter
import net.pterodactylus.sone.utils.Pagination
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * The index page shows the main page of Sone. This page will contain the posts
 * of all friends of the current user.
 */
class IndexPage(template: Template, webInterface: WebInterface, private val postVisibilityFilter: PostVisibilityFilter):
		SoneTemplatePage("index.html", template, "Page.Index.Title", webInterface, true) {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		getCurrentSone(freenetRequest.toadletContext)!!.let { currentSone ->
			(currentSone.posts +
					currentSone.friends
							.mapNotNull(webInterface.core::getSone)
							.flatMap { it.posts } +
					webInterface.core.getDirectedPosts(currentSone.id)
					).distinct()
					.filter { postVisibilityFilter.isVisible(currentSone).apply(it) }
					.sortedByDescending { it.time }
					.let { posts ->
						Pagination(posts, webInterface.core.preferences.postsPerPage).apply {
							page = freenetRequest.parameters["page"]?.toIntOrNull() ?: 0
						}.let { pagination ->
							templateContext["pagination"] = pagination
							templateContext["posts"] = pagination.items
						}
					}
		}
	}

}
