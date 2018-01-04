package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.core.Preferences
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired
import net.pterodactylus.sone.utils.emptyToNull
import net.pterodactylus.sone.utils.isPOST
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext

/**
 * This page lets the user edit the options of the Sone plugin.
 */
class OptionsPage(template: Template, webInterface: WebInterface):
		SoneTemplatePage("options.html", webInterface, template, "Page.Options.Title") {

	override fun handleRequest(freenetRequest: FreenetRequest, templateContext: TemplateContext) {
		if (freenetRequest.isPOST) {
			val fieldsWithErrors = mutableListOf<String>()
			getCurrentSone(freenetRequest.toadletContext)?.options?.let { options ->
				val autoFollow = "auto-follow" in freenetRequest.parameters
				val loadLinkedImages = freenetRequest.parameters["load-linked-images"].emptyToNull
				val showCustomAvatars = freenetRequest.parameters["show-custom-avatars"].emptyToNull
				val enableSoneInsertNotification = "enable-sone-insert-notifications" in freenetRequest.parameters
				val showNewSoneNotification = "show-notification-new-sones" in freenetRequest.parameters
				val showNewPostNotification = "show-notification-new-posts" in freenetRequest.parameters
				val showNewReplyNotification = "show-notification-new-replies" in freenetRequest.parameters

				options.isAutoFollow = autoFollow
				options.isSoneInsertNotificationEnabled = enableSoneInsertNotification
				options.isShowNewSoneNotifications = showNewSoneNotification
				options.isShowNewPostNotifications = showNewPostNotification
				options.isShowNewReplyNotifications = showNewReplyNotification
				loadLinkedImages?.also { if (cantSetOption { options.loadLinkedImages = LoadExternalContent.valueOf(loadLinkedImages) }) fieldsWithErrors += "load-linked-images" }
				showCustomAvatars?.also { if (cantSetOption { options.showCustomAvatars = LoadExternalContent.valueOf(showCustomAvatars) }) fieldsWithErrors += "show-custom-avatars" }
			}
			val fullAccessRequired = "require-full-access" in freenetRequest.parameters
			val fcpInterfaceActive = "fcp-interface-active" in freenetRequest.parameters

			webInterface.core.preferences.isRequireFullAccess = fullAccessRequired
			webInterface.core.preferences.isFcpInterfaceActive = fcpInterfaceActive

			val postsPerPage = freenetRequest.parameters["posts-per-page"]?.toIntOrNull()
			val charactersPerPost = freenetRequest.parameters["characters-per-post"]?.toIntOrNull()
			val postCutOffLength = freenetRequest.parameters["post-cut-off-length"]?.toIntOrNull()
			val imagesPerPage = freenetRequest.parameters["images-per-page"]?.toIntOrNull()
			val insertionDelay = freenetRequest.parameters["insertion-delay"]?.toIntOrNull()
			val fcpFullAccessRequired = freenetRequest.parameters["fcp-full-access-required"]?.toIntOrNull()
			val negativeTrust = freenetRequest.parameters["negative-trust"]?.toIntOrNull()
			val positiveTrust = freenetRequest.parameters["positive-trust"]?.toIntOrNull()
			val trustComment = freenetRequest.parameters["trust-comment"]?.emptyToNull

			if (cantSetOption { it.setPostsPerPage(postsPerPage) }) fieldsWithErrors += "posts-per-page"
			if (cantSetOption { it.setCharactersPerPost(charactersPerPost) }) fieldsWithErrors += "characters-per-post"
			if (cantSetOption { it.setPostCutOffLength(postCutOffLength) }) fieldsWithErrors += "post-cut-off-length"
			if (cantSetOption { it.setImagesPerPage(imagesPerPage) }) fieldsWithErrors += "images-per-page"
			if (cantSetOption { it.setInsertionDelay(insertionDelay) }) fieldsWithErrors += "insertion-delay"
			fcpFullAccessRequired?.also { if (cantSetOption { it.fcpFullAccessRequired = FullAccessRequired.values()[fcpFullAccessRequired] }) fieldsWithErrors += "fcp-full-access-required" }
			if (cantSetOption { it.setNegativeTrust(negativeTrust) }) fieldsWithErrors += "negative-trust"
			if (cantSetOption { it.setPositiveTrust(positiveTrust) }) fieldsWithErrors += "positive-trust"
			if (cantSetOption { it.trustComment = trustComment }) fieldsWithErrors += "trust-comment"

			if (fieldsWithErrors.isEmpty()) {
				webInterface.core.touchConfiguration()
				throw RedirectException("options.html")
			}
			templateContext["fieldErrors"] = fieldsWithErrors
		}
		getCurrentSone(freenetRequest.toadletContext)?.options?.let { options ->
			templateContext["auto-follow"] = options.isAutoFollow
			templateContext["show-notification-new-sones"] = options.isShowNewSoneNotifications
			templateContext["show-notification-new-posts"] = options.isShowNewPostNotifications
			templateContext["show-notification-new-replies"] = options.isShowNewReplyNotifications
			templateContext["enable-sone-insert-notifications"] = options.isSoneInsertNotificationEnabled
			templateContext["load-linked-images"] = options.loadLinkedImages.toString()
			templateContext["show-custom-avatars"] = options.showCustomAvatars.toString()
		}
		webInterface.core.preferences.let { preferences ->
			templateContext["insertion-delay"] = preferences.insertionDelay
			templateContext["characters-per-post"] = preferences.charactersPerPost
			templateContext["fcp-full-access-required"] = preferences.fcpFullAccessRequired.ordinal
			templateContext["images-per-page"] = preferences.imagesPerPage
			templateContext["fcp-interface-active"] = preferences.isFcpInterfaceActive
			templateContext["require-full-access"] = preferences.isRequireFullAccess
			templateContext["negative-trust"] = preferences.negativeTrust
			templateContext["positive-trust"] = preferences.positiveTrust
			templateContext["post-cut-off-length"] = preferences.postCutOffLength
			templateContext["posts-per-page"] = preferences.postsPerPage
			templateContext["trust-comment"] = preferences.trustComment
		}
	}

	private fun cantSetOption(setter: (Preferences) -> Unit) =
			try {
				setter(webInterface.core.preferences)
				false
			} catch (iae: IllegalArgumentException) {
				true
			}

}
