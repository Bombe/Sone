package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * Lets the user delete a post they made.
 */
@TemplatePath("/templates/deletePost.html")
@ToadletPath("deletePost.html")
class DeletePostPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		LoggedInPage("Page.DeletePost.Title", webInterface, loaders, templateRenderer) {

	override fun handleRequest(soneRequest: SoneRequest, currentSone: Sone, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			val post = soneRequest.core.getPost(soneRequest.httpRequest.getPartAsStringFailsafe("post", 36)) ?: redirectTo("noPermission.html")
			val returnPage = soneRequest.httpRequest.getPartAsStringFailsafe("returnPage", 256)
			if (!post.sone.isLocal) {
				redirectTo("noPermission.html")
			}
			if (soneRequest.httpRequest.isPartSet("confirmDelete")) {
				soneRequest.core.deletePost(post)
				redirectTo(returnPage)
			} else if (soneRequest.httpRequest.isPartSet("abortDelete")) {
				redirectTo(returnPage)
			}
			templateContext["post"] = post
			templateContext["returnPage"] = returnPage
			return
		}
		templateContext["post"] = soneRequest.core.getPost(soneRequest.httpRequest.getParam("post")) ?: redirectTo("noPermission.html")
		templateContext["returnPage"] = soneRequest.httpRequest.getParam("returnPage")
	}

}
