package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.template.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import java.net.*
import javax.inject.*

/**
 * Lets the user browser another Sone.
 */
@TemplatePath("/templates/viewSone.html")
@ToadletPath("viewSone.html")
class ViewSonePage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		SoneTemplatePage("viewSone.html", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		templateContext["soneId"] = soneRequest.parameters["sone"]
		soneRequest.parameters["sone"]!!.let(soneRequest.core::getSone)?.let { sone ->
			templateContext["sone"] = sone
			val sonePosts = sone.posts
			val directedPosts = soneRequest.core.getDirectedPosts(sone.id)
			(sonePosts + directedPosts)
					.sortedByDescending(Post::getTime)
					.paginate(soneRequest.core.preferences.postsPerPage)
					.apply { page = soneRequest.parameters["postPage"]?.toIntOrNull() ?: 0 }
					.also {
						templateContext["postPagination"] = it
						templateContext["posts"] = it.items
					}
			sone.replies
					.mapPresent(PostReply::getPost)
					.distinct()
					.minus(sonePosts)
					.minus(directedPosts)
					.sortedByDescending { soneRequest.core.getReplies(it.id).first().time }
					.paginate(soneRequest.core.preferences.postsPerPage)
					.apply { page = soneRequest.parameters["repliedPostPage"]?.toIntOrNull() ?: 0 }
					.also {
						templateContext["repliedPostPagination"] = it
						templateContext["repliedPosts"] = it.items
					}
		}
	}

	override fun isLinkExcepted(link: URI) = true

	override fun getPageTitle(soneRequest: SoneRequest): String =
			soneRequest.parameters["sone"]!!.let(soneRequest.core::getSone)?.let { sone ->
				"${SoneAccessor.getNiceName(sone)} - ${soneRequest.l10n.getString("Page.ViewSone.Title")}"
			} ?: soneRequest.l10n.getString("Page.ViewSone.Page.TitleWithoutSone")

}
