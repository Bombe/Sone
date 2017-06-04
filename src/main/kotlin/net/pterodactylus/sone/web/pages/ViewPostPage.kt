package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.template.SoneAccessor
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import java.net.URI

/**
 * This page lets the user view a post and all its replies.
 */
class ViewPostPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("viewPost.html", template, "Page.ViewPost.Title", webInterface, false) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		templateContext["post"] = request.parameters["post"].let(webInterface.core::getPost).orNull()
		templateContext["raw"] = request.parameters["raw"] == "true"
	}

	override fun isLinkExcepted(link: URI?) = true

	public override fun getPageTitle(request: FreenetRequest) =
			(request.parameters["post"].let(webInterface.core::getPost).let {
				if (it.text.length > 20) {
					it.text.substring(0..19) + "…"
				} else {
					it.text
				} + " - ${SoneAccessor.getNiceName(it.sone)} - "
			} ?: "") + super.getPageTitle(request)

}
