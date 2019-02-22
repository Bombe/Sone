package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Page that lets the user mark a number of [net.pterodactylus.sone.data.Sone]s, [Post]s, or
 * [Replie][net.pterodactylus.sone.data.Reply]s as known.
 */
class MarkAsKnownPage @Inject constructor(template: Template, webInterface: WebInterface):
		SoneTemplatePage("markAsKnown.html", webInterface, template, "Page.MarkAsKnown.Title") {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		val ids = freenetRequest.parameters["id", 65536]!!.split(" ")
		when (freenetRequest.parameters["type", 5]) {
			"sone" -> ids.mapNotNull(webInterface.core::getSone).forEach(webInterface.core::markSoneKnown)
			"post" -> ids.mapNotNull(webInterface.core::getPost).forEach(webInterface.core::markPostKnown)
			"reply" -> ids.mapNotNull(webInterface.core::getPostReply).forEach(webInterface.core::markReplyKnown)
			else -> throw RedirectException("invalid.html")
		}
		throw RedirectException(freenetRequest.parameters["returnPage", 256]!!)
	}

}
