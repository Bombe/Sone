package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Page that lets the user unlike a [net.pterodactylus.sone.data.Post] or [net.pterodactylus.sone.data.Reply].
 */
@ToadletPath("unlike.html")
class UnlikePage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("Page.Unlike.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			when (soneRequest.parameters["type"]) {
				"post" -> currentSone.removeLikedPostId(soneRequest.parameters["post"]!!)
				"reply" -> currentSone.removeLikedReplyId(soneRequest.parameters["reply"]!!)
			}
			throw RedirectException(soneRequest.parameters["returnPage", 256])
		}
	}

}
