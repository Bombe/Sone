package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import javax.inject.Inject

/**
 * Lets the user delete a post they made.
 */
@TemplatePath("/templates/deletePost.html")
class DeletePostPage @Inject constructor(template: Template, webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer):
		LoggedInPage("deletePost.html", template, "Page.DeletePost.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			val post = soneRequest.core.getPost(soneRequest.httpRequest.getPartAsStringFailsafe("post", 36)) ?: throw RedirectException("noPermission.html")
			val returnPage = soneRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256)
			if (!post.sone.isLocal) {
				throw RedirectException("noPermission.html")
			}
			if (soneRequest.httpRequest.isPartSet("confirmDelete")) {
				soneRequest.core.deletePost(post)
				throw RedirectException(returnPage)
			} else if (soneRequest.httpRequest.isPartSet("abortDelete")) {
				throw RedirectException(returnPage)
			}
			templateContext["post"] = post
			templateContext["returnPage"] = returnPage
			return
		}
		templateContext["post"] = soneRequest.core.getPost(soneRequest.httpRequest.getParam("post")) ?: throw RedirectException("noPermission.html")
		templateContext["returnPage"] = soneRequest.httpRequest.getParam("returnPage")
	}

}
