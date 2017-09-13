package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.jsonObject
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.utils.render
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template

/**
 * This AJAX handler retrieves information and rendered representation of a [Post].
 */
class GetPostAjaxPage(webInterface: WebInterface, private val postTemplate: Template) : LoggedInJsonPage("getPost.ajax", webInterface) {

	override fun needsFormPassword() = false

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			request.parameters["post"]
					.let(webInterface.core::getPost)
					.let { post ->
						createSuccessJsonObject().
								put("post", jsonObject(
										"id" to post.id,
										"sone" to post.sone.id,
										"time" to post.time,
										"recipient" to post.recipientId.orNull(),
										"html" to post.render(currentSone, request)
								))
					} ?: createErrorJsonObject("invalid-post-id")

	private fun Post.render(currentSone: Sone, request: FreenetRequest) =
			webInterface.templateContextFactory.createTemplateContext().apply {
				set("core", webInterface.core)
				set("request", request)
				set("post", this@render)
				set("currentSone", currentSone)
				set("localSones", webInterface.core.localSones)
			}.let { postTemplate.render(it) }

}
