package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.SoneOptions
import net.pterodactylus.sone.main.SonePlugin
import net.pterodactylus.sone.utils.jsonArray
import net.pterodactylus.sone.utils.jsonObject
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.notify.Notification
import net.pterodactylus.util.notify.TemplateNotification
import java.io.StringWriter

/**
 * AJAX handler to return all current notifications.
 */
class GetNotificationsAjaxPage(webInterface: WebInterface) : JsonPage("getNotifications.ajax", webInterface) {

	override val needsFormPassword = false
	override val requiresLogin = false

	override fun createJsonObject(request: FreenetRequest) =
			getCurrentSone(request.toadletContext, false).let { currentSone ->
				webInterface.getNotifications(currentSone)
						.sortedBy(Notification::getCreatedTime)
						.let { notifications ->
							createSuccessJsonObject().apply {
								put("notificationHash", notifications.hashCode())
								put("options", currentSone?.options.asJsonObject)
								put("notifications", notifications.asJsonObject(currentSone, request))
							}
						}
			}

	private fun Collection<Notification>.asJsonObject(currentSone: Sone?, freenetRequest: FreenetRequest) = jsonArray(
			*map { notification ->
				jsonObject(
						"id" to notification.id,
						"createdTime" to notification.createdTime,
						"lastUpdatedTime" to notification.lastUpdatedTime,
						"dismissable" to notification.isDismissable,
						"text" to if (notification is TemplateNotification) notification.render(currentSone, freenetRequest) else notification.render()
				)
			}.toTypedArray()
	)

	private fun TemplateNotification.render(currentSone: Sone?, freenetRequest: FreenetRequest) = StringWriter().use {
		val mergedTemplateContext = webInterface.templateContextFactory.createTemplateContext()
				.mergeContext(templateContext)
				.apply {
					this["core"] = core
					this["currentSone"] = currentSone
					this["localSones"] = core.localSones
					this["request"] = freenetRequest
					this["currentVersion"] = SonePlugin.getPluginVersion()
					this["hasLatestVersion"] = core.updateChecker.hasLatestVersion()
					this["latestEdition"] = core.updateChecker.latestEdition
					this["latestVersion"] = core.updateChecker.latestVersion
					this["latestVersionTime"] = core.updateChecker.latestVersionDate
					this["notification"] = this@render
				}
		it.also { render(mergedTemplateContext, it) }
	}.toString()

}

private val SoneOptions?.asJsonObject
	get() = this?.let { options ->
		jsonObject(
				"ShowNotification/NewSones" to options.isShowNewSoneNotifications,
				"ShowNotification/NewPosts" to options.isShowNewPostNotifications,
				"ShowNotification/NewReplies" to options.isShowNewReplyNotifications
		)
	} ?: jsonObject {}

private fun Notification.render() = StringWriter().use { it.also { render(it) } }.toString()
