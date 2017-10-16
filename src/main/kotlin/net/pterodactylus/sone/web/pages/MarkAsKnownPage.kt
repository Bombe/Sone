package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.utils.mapPresent
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * Page that lets the user mark a number of [net.pterodactylus.sone.data.Sone]s, [Post]s, or
 * [Replie][net.pterodactylus.sone.data.Reply]s as known.
 */
class MarkAsKnownPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("markAsKnown.html", template, "Page.MarkAsKnown.Title", webInterface, false) {

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
