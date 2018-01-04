package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.utils.Pagination
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user browse all his bookmarked posts.
 */
class BookmarksPage(template: Template, webInterface: WebInterface) :
		SoneTemplatePage("bookmarks.html", webInterface, template, "Page.Bookmarks.Title") {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		webInterface.core.bookmarkedPosts.let { posts ->
			val pagination = Pagination<Post>(posts.filter { it.isLoaded }.sortedByDescending { it.time }, webInterface.core.preferences.postsPerPage)
			templateContext["pagination"] = pagination
			templateContext["posts"] = pagination.items
			templateContext["postsNotLoaded"] = posts.any { !it.isLoaded }
		}
	}

}
