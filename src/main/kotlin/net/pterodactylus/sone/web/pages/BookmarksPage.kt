package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Page that lets the user browse all his bookmarked posts.
 */
@MenuName("Bookmarks")
@TemplatePath("/templates/bookmarks.html")
@ToadletPath("bookmarks.html")
class BookmarksPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		SoneTemplatePage("bookmarks.html", webInterface, loaders, templateRenderer, pageTitleKey = "Page.Bookmarks.Title") {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		soneRequest.core.bookmarkedPosts.let { posts ->
			val pagination = posts.filter(Post::isLoaded).sortedByDescending { it.time }.paginate(soneRequest.core.preferences.postsPerPage)
			templateContext["pagination"] = pagination
			templateContext["posts"] = pagination.items
			templateContext["postsNotLoaded"] = posts.any { !it.isLoaded }
		}
	}

}
