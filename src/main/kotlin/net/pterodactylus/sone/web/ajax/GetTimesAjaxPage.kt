package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.freenet.L10nFilter
import net.pterodactylus.sone.text.TimeTextConverter
import net.pterodactylus.sone.utils.jsonObject
import net.pterodactylus.sone.utils.let
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import java.text.SimpleDateFormat
import java.util.TimeZone

/**
 * Ajax page that returns a formatted, relative timestamp for replies or posts.
 */
class GetTimesAjaxPage(webInterface: WebInterface,
		private val timeTextConverter: TimeTextConverter,
		private val l10nFilter: L10nFilter,
		timeZone: TimeZone) : JsonPage("getTimes.ajax", webInterface) {

	private val dateTimeFormatter = SimpleDateFormat("MMM d, yyyy, HH:mm:ss").apply {
		this.timeZone = timeZone
	}

	override fun needsFormPassword() = false
	override fun requiresLogin() = false

	override fun createJsonObject(request: FreenetRequest) =
			createSuccessJsonObject().apply {
				put("postTimes", request.parameters["posts"]!!.idsToJson { webInterface.core.getPost(it)?.let { it.id to it.time } })
				put("replyTimes", request.parameters["replies"]!!.idsToJson { webInterface.core.getPostReply(it)?.let { it.id to it.time } })
			}

	private fun String.idsToJson(transform: (String) -> Pair<String, Long>?) =
			split(",").mapNotNull(transform).toJson()

	private fun List<Pair<String, Long>>.toJson() = jsonObject {
		this@toJson.map { (id, time) ->
			val timeText = timeTextConverter.getTimeText(time)
			id to jsonObject(
					"timeText" to l10nFilter.format(null, timeText.l10nText, emptyMap()),
					"refreshTime" to timeText.refreshTime / 1000,
					"tooltip" to synchronized(dateTimeFormatter) {
						dateTimeFormatter.format(time)
					})
		}.forEach { this@jsonObject.put(it.first, it.second) }
	}

}
