package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Page that lets the user mark a number of [net.pterodactylus.sone.data.Sone]s, [Post]s, or
 * [Replie][net.pterodactylus.sone.data.Reply]s as known.
 */
@ToadletPath("markAsKnown.html")
class MarkAsKnownPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		SoneTemplatePage(webInterface, loaders, templateRenderer, pageTitleKey = "Page.MarkAsKnown.Title") {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		val ids = soneRequest.parameters["id", 65536]!!.split(" ")
		when (soneRequest.parameters["type", 5]) {
			"sone" -> ids.mapNotNull(soneRequest.core::getSone).forEach(soneRequest.core::markSoneKnown)
			"post" -> ids.mapNotNull(soneRequest.core::getPost).forEach(soneRequest.core::markPostKnown)
			"reply" -> ids.mapNotNull(soneRequest.core::getPostReply).forEach(soneRequest.core::markReplyKnown)
			else -> redirectTo("invalid.html")
		}
		redirectTo(soneRequest.parameters["returnPage", 256]!!)
	}

}
