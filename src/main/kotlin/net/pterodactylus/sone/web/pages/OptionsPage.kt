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
		SoneTemplatePage("options.html", template, "Page.Options.Title", webInterface, false) {

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		if (request.isPOST) {
			val fieldsWithErrors = mutableListOf<String>()
			getCurrentSone(request.toadletContext)?.options?.let { options ->
				val autoFollow = "auto-follow" in request.parameters
				val loadLinkedImages = request.parameters["load-linked-images"].emptyToNull
				val showCustomAvatars = request.parameters["show-custom-avatars"].emptyToNull
				val enableSoneInsertNotification = "enable-sone-insert-notifications" in request.parameters
				val showNewSoneNotification = "show-notification-new-sones" in request.parameters
				val showNewPostNotification = "show-notification-new-posts" in request.parameters
				val showNewReplyNotification = "show-notification-new-replies" in request.parameters

				options.isAutoFollow = autoFollow
				options.isSoneInsertNotificationEnabled = enableSoneInsertNotification
				options.isShowNewSoneNotifications = showNewSoneNotification
				options.isShowNewPostNotifications = showNewPostNotification
				options.isShowNewReplyNotifications = showNewReplyNotification
				loadLinkedImages?.also { if (cantSetOption { options.loadLinkedImages = LoadExternalContent.valueOf(loadLinkedImages) }) fieldsWithErrors += "load-linked-images" }
				showCustomAvatars?.also { if (cantSetOption { options.showCustomAvatars = LoadExternalContent.valueOf(showCustomAvatars) }) fieldsWithErrors += "show-custom-avatars" }
			}
			val fullAccessRequired = "require-full-access" in request.parameters
			val fcpInterfaceActive = "fcp-interface-active" in request.parameters

			webInterface.core.preferences.isRequireFullAccess = fullAccessRequired
			webInterface.core.preferences.isFcpInterfaceActive = fcpInterfaceActive

			val postsPerPage = request.parameters["posts-per-page"]?.toIntOrNull()
			val charactersPerPost = request.parameters["characters-per-post"]?.toIntOrNull()
			val postCutOffLength = request.parameters["post-cut-off-length"]?.toIntOrNull()
			val imagesPerPage = request.parameters["images-per-page"]?.toIntOrNull()
			val insertionDelay = request.parameters["insertion-delay"]?.toIntOrNull()
			val fcpFullAccessRequired = request.parameters["fcp-full-access-required"]?.toIntOrNull()
			val negativeTrust = request.parameters["negative-trust"]?.toIntOrNull()
			val positiveTrust = request.parameters["positive-trust"]?.toIntOrNull()
			val trustComment = request.parameters["trust-comment"]?.emptyToNull

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
		getCurrentSone(request.toadletContext)?.options?.let { options ->
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
