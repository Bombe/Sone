package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.template.SoneAccessor
import net.pterodactylus.sone.utils.mapPresent
import net.pterodactylus.sone.utils.paginate
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import java.net.URI
import javax.inject.Inject

/**
 * Lets the user browser another Sone.
 */
class ViewSonePage @Inject constructor(template: Template, webInterface: WebInterface):
		SoneTemplatePage("viewSone.html", webInterface, template) {

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
