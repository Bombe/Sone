package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.utils.paginate
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Page that lets the user browse all his bookmarked posts.
 */
@MenuName("Bookmarks")
class BookmarksPage @Inject constructor(template: Template, webInterface: WebInterface) :
		SoneTemplatePage("bookmarks.html", webInterface, template, "Page.Bookmarks.Title") {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		soneRequest.core.bookmarkedPosts.let { posts ->
			val pagination = posts.filter(Post::isLoaded).sortedByDescending { it.time }.paginate(soneRequest.core.preferences.postsPerPage)
			templateContext["pagination"] = pagination
			templateContext["posts"] = pagination.items
			templateContext["postsNotLoaded"] = posts.any { !it.isLoaded }
		}
	}

}
