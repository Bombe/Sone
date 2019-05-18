package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.jsonObject
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.utils.render
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import javax.inject.Inject

/**
 * This AJAX handler retrieves information and rendered representation of a [Post].
 */
@ToadletPath("getPost.ajax")
class GetPostAjaxPage @Inject constructor(webInterface: WebInterface, private val postTemplate: Template) : LoggedInJsonPage(webInterface) {

	override val needsFormPassword = false

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			request.parameters["post"]
					?.let(core::getPost)
					?.let { post ->
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
				set("core", core)
				set("request", request)
				set("post", this@render)
				set("currentSone", currentSone)
				set("localSones", core.localSones)
			}.let { postTemplate.render(it) }

}
