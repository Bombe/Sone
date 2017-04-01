package net.pterodactylus.sone.web.ajax

import com.fasterxml.jackson.databind.JsonNode
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
import net.pterodactylus.sone.web.page.FreenetRequest
import java.text.SimpleDateFormat

/**
 * The “get status” AJAX handler returns all information that is necessary to
 * update the web interface in real-time.
 */
class GetStatusAjaxPage(webInterface: WebInterface, private val timeTextConverter: TimeTextConverter, private val l10nFilter: L10nFilter):
		JsonPage("getStatus.ajax", webInterface) {

	private val dateFormatter = SimpleDateFormat("MMM d, yyyy, HH:mm:ss")

	override fun createJsonObject(request: FreenetRequest) =
			(webInterface.getCurrentSoneWithoutCreatingSession(request.toadletContext) as Sone?).let { currentSone ->
				createSuccessJsonObject().apply {
					this["loggedIn"] = currentSone != null
					this["options"] = currentSone?.options?.toJsonOptions() ?: jsonObject {}
					this["notificationHash"] = webInterface.getNotifications(currentSone).sortedBy { it.createdTime }.hashCode()
					this["sones"] = request.httpRequest.getParam("soneIds").split(',').map { webInterface.core.getSone(it).orNull() }.plus(currentSone).filterNotNull().toJsonSones()
					this["newPosts"] = webInterface.getNewPosts(currentSone).toJsonPosts()
					this["newReplies"] = webInterface.getNewReplies(currentSone).toJsonReplies()
				}
			}

	private operator fun JsonReturnObject.set(key: String, value: JsonNode) = put(key, value)
	private operator fun JsonReturnObject.set(key: String, value: Int) = put(key, value)
	private operator fun JsonReturnObject.set(key: String, value: Boolean) = put(key, value)

	override fun needsFormPassword() = false
	override fun requiresLogin() = false

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
			put("modified", webInterface.core.isModifiedSone(sone))
			put("locked", webInterface.core.isLocked(sone))
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

}
