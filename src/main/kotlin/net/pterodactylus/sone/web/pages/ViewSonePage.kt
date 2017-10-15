package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.template.SoneAccessor
import net.pterodactylus.sone.utils.mapPresent
import net.pterodactylus.sone.utils.paginate
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import java.net.URI

/**
 * Lets the user browser another Sone.
 */
class ViewSonePage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("viewSone.html", template, webInterface, false) {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		templateContext["soneId"] = freenetRequest.parameters["sone"]
		freenetRequest.parameters["sone"]!!.let(webInterface.core::getSone)?.let { sone ->
			templateContext["sone"] = sone
			val sonePosts = sone.posts
			val directedPosts = webInterface.core.getDirectedPosts(sone.id)
			(sonePosts + directedPosts)
					.sortedByDescending(Post::getTime)
					.paginate(webInterface.core.preferences.postsPerPage)
					.apply { page = freenetRequest.parameters["postPage"]?.toIntOrNull() ?: 0 }
					.also {
						templateContext["postPagination"] = it
						templateContext["posts"] = it.items
					}
			sone.replies
					.mapPresent(PostReply::getPost)
					.distinct()
					.minus(sonePosts)
					.minus(directedPosts)
					.sortedByDescending { webInterface.core.getReplies(it.id).first().time }
					.paginate(webInterface.core.preferences.postsPerPage)
					.apply { page = freenetRequest.parameters["repliedPostPage"]?.toIntOrNull() ?: 0 }
					.also {
						templateContext["repliedPostPagination"] = it
						templateContext["repliedPosts"] = it.items
					}
		}
	}

	override fun isLinkExcepted(link: URI?) = true

	public override fun getPageTitle(freenetRequest: FreenetRequest): String =
			freenetRequest.parameters["sone"]!!.let(webInterface.core::getSone)?.let { sone ->
				"${SoneAccessor.getNiceName(sone)} - ${webInterface.l10n.getString("Page.ViewSone.Title")}"
			} ?: webInterface.l10n.getString("Page.ViewSone.Page.TitleWithoutSone")

}
