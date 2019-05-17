package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.notify.PostVisibilityFilter
import net.pterodactylus.sone.utils.paginate
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * The index page shows the main page of Sone. This page will contain the posts
 * of all friends of the current user.
 */
@MenuName("Index")
@TemplatePath("/templates/index.html")
class IndexPage @Inject constructor(template: Template, webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer, private val postVisibilityFilter: PostVisibilityFilter) :
		LoggedInPage("index.html", template, "Page.Index.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		(currentSone.posts +
				currentSone.friends
						.mapNotNull(soneRequest.core::getSone)
						.flatMap { it.posts } +
				soneRequest.core.getDirectedPosts(currentSone.id)
				).distinct()
				.filter { postVisibilityFilter.isVisible(currentSone).apply(it) }
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
