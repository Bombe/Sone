package net.pterodactylus.sone.web.ajax

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import net.pterodactylus.sone.core.ElementLoader
import net.pterodactylus.sone.core.LinkedElement
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.SoneOptions
import net.pterodactylus.sone.freenet.L10nFilter
import net.pterodactylus.sone.template.SoneAccessor
import net.pterodactylus.sone.text.TimeTextConverter
import net.pterodactylus.sone.utils.jsonObject
import net.pterodactylus.sone.utils.toArray
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.*
import java.text.SimpleDateFormat
import java.util.TimeZone
import javax.inject.Inject

/**
 * The “get status” AJAX handler returns all information that is necessary to
 * update the web interface in real-time.
 */
@ToadletPath("getStatus.ajax")
class GetStatusAjaxPage(webInterface: WebInterface, private val elementLoader: ElementLoader, private val timeTextConverter: TimeTextConverter, private val l10nFilter: L10nFilter, timeZone: TimeZone):
		JsonPage(webInterface) {

	@Inject constructor(webInterface: WebInterface, elementLoader: ElementLoader, timeTextConverter: TimeTextConverter, l10nFilter: L10nFilter):
			this(webInterface, elementLoader, timeTextConverter, l10nFilter, TimeZone.getDefault())

	private val dateFormatter = SimpleDateFormat("MMM d, yyyy, HH:mm:ss").apply {
		this.timeZone = timeZone
	}

	override fun createJsonObject(request: FreenetRequest) =
			getCurrentSone(request.toadletContext, false).let { currentSone ->
				createSuccessJsonObject().apply {
					this["loggedIn"] = currentSone != null
					this["options"] = currentSone?.options?.toJsonOptions() ?: jsonObject {}
					this["notificationHash"] = webInterface.getNotifications(currentSone).sortedBy { it.createdTime }.hashCode()
					this["sones"] = request.httpRequest.getParam("soneIds").split(',').mapNotNull(core::getSone).plus(currentSone).filterNotNull().toJsonSones()
					this["newPosts"] = webInterface.getNewPosts(currentSone).toJsonPosts()
					this["newReplies"] = webInterface.getNewReplies(currentSone).toJsonReplies()
					this["linkedElements"] = request.httpRequest.getParam("elements", "[]").asJson().map(JsonNode::asText).map(elementLoader::loadElement).toJsonElements()
				}
			}

	private operator fun JsonReturnObject.set(key: String, value: JsonNode) = put(key, value)
	private operator fun JsonReturnObject.set(key: String, value: Int) = put(key, value)
	private operator fun JsonReturnObject.set(key: String, value: Boolean) = put(key, value)

	private fun String.asJson() = ObjectMapper().readTree(this).asIterable()

	override val needsFormPassword = false
	override val requiresLogin = false

	private fun SoneOptions.toJsonOptions() = jsonObject {
		put("ShowNotification/NewSones", isShowNewSoneNotifications)
		put("ShowNotification/NewPosts", isShowNewPostNotifications)
		put("ShowNotification/NewReplies", isShowNewReplyNotifications)
	}

	private fun Iterable<Sone>.toJsonSones() = map { sone ->
		jsonObject {
			put("id", sone.id)
			put("name", SoneAccessor.getNiceName(sone))
			put("local", sone.isLocal)
			put("status", sone.status.name)
			put("modified", core.isModifiedSone(sone))
			put("locked", core.isLocked(sone))
			put("lastUpdatedUnknown", sone.time == 0L)
			synchronized(dateFormatter) {
				put("lastUpdated", dateFormatter.format(sone.time))
			}
			put("lastUpdatedText", timeTextConverter.getTimeText(sone.time).l10nText.let { l10nFilter.format(null, it, emptyMap()) })
		}
	}.toArray()

	private fun Iterable<Post>.toJsonPosts() = map { post ->
		jsonObject {
			put("id", post.id)
			put("sone", post.sone.id)
			put("time", post.time)
			put("recipient", post.recipientId.orNull())
		}
	}.toArray()

	private fun Iterable<PostReply>.toJsonReplies() = map { reply ->
		jsonObject {
			put("id", reply.id)
			put("sone", reply.sone.id)
			put("post", reply.postId)
			put("postSone", reply.post.get().sone.id)
		}
	}.toArray()

	private fun Iterable<LinkedElement>.toJsonElements() = map { (link, failed, loading) ->
		jsonObject {
			put("link", link)
			put("loading", loading)
			put("failed", failed)
		}
	}.toArray()

}
