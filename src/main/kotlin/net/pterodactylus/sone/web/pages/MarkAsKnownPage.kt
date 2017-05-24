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

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		val ids = request.parameters["id", 65536]!!.split(" ")
		when (request.parameters["type", 5]) {
			"sone" -> ids.mapPresent(webInterface.core::getSone).forEach(webInterface.core::markSoneKnown)
			"post" -> ids.mapPresent(webInterface.core::getPost).forEach(webInterface.core::markPostKnown)
			"reply" -> ids.mapPresent(webInterface.core::getPostReply).forEach(webInterface.core::markReplyKnown)
			else -> throw RedirectException("invalid.html")
		}
		throw RedirectException(request.parameters["returnPage", 256]!!)
	}

}
