package net.pterodactylus.sone.web.pages

import net.pterodactylus.sone.data.SoneOptions.*
import net.pterodactylus.sone.fcp.FcpInterface.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.sone.web.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.template.*
import javax.inject.*

/**
 * This page lets the user edit the options of the Sone plugin.
 */
@MenuName("Options")
@TemplatePath("/templates/options.html")
@ToadletPath("options.html")
class OptionsPage @Inject constructor(webInterface: WebInterface, loaders: Loaders, templateRenderer: TemplateRenderer) :
		SoneTemplatePage(webInterface, loaders, templateRenderer, pageTitleKey = "Page.Options.Title") {

	override fun handleRequest(soneRequest: SoneRequest, templateContext: TemplateContext) {
		if (soneRequest.isPOST) {
			val fieldsWithErrors = mutableListOf<String>()
			getCurrentSone(soneRequest.toadletContext)?.options?.let { options ->
				val autoFollow = "auto-follow" in soneRequest.parameters
				val loadLinkedImages = soneRequest.parameters["load-linked-images"].emptyToNull
				val showCustomAvatars = soneRequest.parameters["show-custom-avatars"].emptyToNull
				val enableSoneInsertNotification = "enable-sone-insert-notifications" in soneRequest.parameters
				val showNewSoneNotification = "show-notification-new-sones" in soneRequest.parameters
				val showNewPostNotification = "show-notification-new-posts" in soneRequest.parameters
				val showNewReplyNotification = "show-notification-new-replies" in soneRequest.parameters

				options.isAutoFollow = autoFollow
				options.isSoneInsertNotificationEnabled = enableSoneInsertNotification
				options.isShowNewSoneNotifications = showNewSoneNotification
				options.isShowNewPostNotifications = showNewPostNotification
				options.isShowNewReplyNotifications = showNewReplyNotification
				loadLinkedImages?.also { if (cantSetOption { options.loadLinkedImages = LoadExternalContent.valueOf(loadLinkedImages) }) fieldsWithErrors += "load-linked-images" }
				showCustomAvatars?.also { if (cantSetOption { options.showCustomAvatars = LoadExternalContent.valueOf(showCustomAvatars) }) fieldsWithErrors += "show-custom-avatars" }
			}
			val fullAccessRequired = "require-full-access" in soneRequest.parameters
			val fcpInterfaceActive = "fcp-interface-active" in soneRequest.parameters

			soneRequest.core.preferences.newRequireFullAccess = fullAccessRequired
			soneRequest.core.preferences.newFcpInterfaceActive = fcpInterfaceActive

			val postsPerPage = soneRequest.parameters["posts-per-page"]?.toIntOrNull()
			val charactersPerPost = soneRequest.parameters["characters-per-post"]?.toIntOrNull()
			val postCutOffLength = soneRequest.parameters["post-cut-off-length"]?.toIntOrNull()
			val imagesPerPage = soneRequest.parameters["images-per-page"]?.toIntOrNull()
			val insertionDelay = soneRequest.parameters["insertion-delay"]?.toIntOrNull()
			val fcpFullAccessRequired = soneRequest.parameters["fcp-full-access-required"]?.toIntOrNull()
			val negativeTrust = soneRequest.parameters["negative-trust"]?.toIntOrNull()
			val positiveTrust = soneRequest.parameters["positive-trust"]?.toIntOrNull()
			val trustComment = soneRequest.parameters["trust-comment"]?.emptyToNull

			if (cantSetOption { soneRequest.core.preferences.newPostsPerPage = postsPerPage }) fieldsWithErrors += "posts-per-page"
			if (cantSetOption { soneRequest.core.preferences.newCharactersPerPost = charactersPerPost }) fieldsWithErrors += "characters-per-post"
			if (cantSetOption { soneRequest.core.preferences.newPostCutOffLength = postCutOffLength }) fieldsWithErrors += "post-cut-off-length"
			if (cantSetOption { soneRequest.core.preferences.newImagesPerPage = imagesPerPage }) fieldsWithErrors += "images-per-page"
			if (cantSetOption { soneRequest.core.preferences.newInsertionDelay = insertionDelay }) fieldsWithErrors += "insertion-delay"
			fcpFullAccessRequired?.also { if (cantSetOption { soneRequest.core.preferences.newFcpFullAccessRequired = FullAccessRequired.values()[fcpFullAccessRequired] }) fieldsWithErrors += "fcp-full-access-required" }
			if (cantSetOption { soneRequest.core.preferences.newNegativeTrust = negativeTrust }) fieldsWithErrors += "negative-trust"
			if (cantSetOption { soneRequest.core.preferences.newPositiveTrust = positiveTrust }) fieldsWithErrors += "positive-trust"
			if (cantSetOption { soneRequest.core.preferences.newTrustComment = trustComment }) fieldsWithErrors += "trust-comment"

			if (fieldsWithErrors.isEmpty()) {
				soneRequest.core.touchConfiguration()
				throw RedirectException("options.html")
			}
			templateContext["fieldErrors"] = fieldsWithErrors
		}
		getCurrentSone(soneRequest.toadletContext)?.options?.let { options ->
			templateContext["auto-follow"] = options.isAutoFollow
			templateContext["show-notification-new-sones"] = options.isShowNewSoneNotifications
			templateContext["show-notification-new-posts"] = options.isShowNewPostNotifications
			templateContext["show-notification-new-replies"] = options.isShowNewReplyNotifications
			templateContext["enable-sone-insert-notifications"] = options.isSoneInsertNotificationEnabled
			templateContext["load-linked-images"] = options.loadLinkedImages.toString()
			templateContext["show-custom-avatars"] = options.showCustomAvatars.toString()
		}
		soneRequest.core.preferences.let { preferences ->
			templateContext["insertion-delay"] = preferences.insertionDelay
			templateContext["characters-per-post"] = preferences.charactersPerPost
			templateContext["fcp-full-access-required"] = preferences.fcpFullAccessRequired.ordinal
			templateContext["images-per-page"] = preferences.imagesPerPage
			templateContext["fcp-interface-active"] = preferences.fcpInterfaceActive
			templateContext["require-full-access"] = preferences.requireFullAccess
			templateContext["negative-trust"] = preferences.negativeTrust
			templateContext["positive-trust"] = preferences.positiveTrust
			templateContext["post-cut-off-length"] = preferences.postCutOffLength
			templateContext["posts-per-page"] = preferences.postsPerPage
			templateContext["trust-comment"] = preferences.trustComment
		}
	}

	private fun cantSetOption(setter: () -> Unit) =
			try {
				setter()
				false
			} catch (iae: IllegalArgumentException) {
				true
			}

}
