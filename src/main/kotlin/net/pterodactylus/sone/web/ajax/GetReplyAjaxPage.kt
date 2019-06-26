package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.jsonObject
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.utils.render
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.Template
import javax.inject.Inject

/**
 * This AJAX page returns the details of a reply.
 */
@ToadletPath("getReply.ajax")
class GetReplyAjaxPage @Inject constructor(webInterface: WebInterface, private val template: Template) : LoggedInJsonPage(webInterface) {

	override val needsFormPassword = false

	override fun createJsonObject(currentSone: Sone, request: FreenetRequest) =
			request.parameters["reply"]
					?.let(core::getPostReply)
					?.let { it.toJson(currentSone, request) }
					?.let { replyJson ->
						createSuccessJsonObject().apply {
							put("reply", replyJson)
						}
					} ?: createErrorJsonObject("invalid-reply-id")

	private fun PostReply.toJson(currentSone: Sone, request: FreenetRequest) = jsonObject(*mapOf(
			"id" to id,
			"soneId" to sone.id,
			"postId" to postId,
			"time" to time,
			"html" to render(currentSone, request)
	).toList().toTypedArray())

	private fun PostReply.render(currentSone: Sone, request: FreenetRequest) =
			webInterface.templateContextFactory.createTemplateContext().apply {
				set("core", core)
				set("request", request)
				set("reply", this@render)
				set("currentSone", currentSone)
			}.let { template.render(it) }

}
