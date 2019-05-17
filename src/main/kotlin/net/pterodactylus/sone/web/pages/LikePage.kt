package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Page that lets the user like [net.pterodactylus.sone.data.Post]s and [net.pterodactylus.sone.data.Reply]s.
 */
@ToadletPath("like.html")
class LikePage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("like.html", "Page.Like.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			soneRequest.parameters["type", 16]?.also { type ->
				when (type) {
					"post" -> currentSone.addLikedPostId(soneRequest.parameters["post", 36]!!)
					"reply" -> currentSone.addLikedReplyId(soneRequest.parameters["reply", 36]!!)
				}
			}
			throw RedirectException(soneRequest.parameters["returnPage", 256]!!)
		}
	}

}
