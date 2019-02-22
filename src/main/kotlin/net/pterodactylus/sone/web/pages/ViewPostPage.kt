package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.template.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import java.net.*
import javax.inject.*

/**
 * This page lets the user view a post and all its replies.
 */
class ViewPostPage @Inject constructor(template: Template, webInterface: WebInterface):
		SoneTemplatePage("viewPost.html", webInterface, template, "Page.ViewPost.Title") {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		templateContext["post"] = freenetRequest.parameters["post"]?.let(webInterface.core::getPost)
		templateContext["raw"] = freenetRequest.parameters["raw"] == "true"
	}

	override fun isLinkExcepted(link: URI?) = true

	override fun getPageTitle(freenetRequest: FreenetRequest) =
			(freenetRequest.parameters["post"]?.let(webInterface.core::getPost)?.let {
				if (it.text.length > 20) {
					it.text.substring(0..19) + "…"
				} else {
					it.text
				} + " - ${SoneAccessor.getNiceName(it.sone)} - "
			} ?: "") + super.getPageTitle(freenetRequest)

}
