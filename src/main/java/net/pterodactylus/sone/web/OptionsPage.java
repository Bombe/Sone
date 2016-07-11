/*
 * Sone - OptionsPage.java - Copyright © 2010–2016 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.web;

import static net.pterodactylus.sone.utils.NumberParsers.parseInt;

import java.util.ArrayList;
import java.util.List;

import net.pterodactylus.sone.core.Preferences;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.Sone.ShowCustomAvatars;
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;

/**
 * This page lets the user edit the options of the Sone plugin.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class OptionsPage extends SoneTemplatePage {

	/**
	 * Creates a new options page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public OptionsPage(Template template, WebInterface webInterface) {
		super("options.html", template, "Page.Options.Title", webInterface, false);
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		Preferences preferences = webInterface.getCore().getPreferences();
		Sone currentSone = webInterface.getCurrentSone(request.getToadletContext(), false);
		if (request.getMethod() == Method.POST) {
			List<String> fieldErrors = new ArrayList<String>();
			if (currentSone != null) {
				boolean autoFollow = request.getHttpRequest().isPartSet("auto-follow");
				currentSone.getOptions().setAutoFollow(autoFollow);
				boolean enableSoneInsertNotifications = request.getHttpRequest().isPartSet("enable-sone-insert-notifications");
				currentSone.getOptions().setSoneInsertNotificationEnabled(enableSoneInsertNotifications);
				boolean showNotificationNewSones = request.getHttpRequest().isPartSet("show-notification-new-sones");
				currentSone.getOptions().setShowNewSoneNotifications(showNotificationNewSones);
				boolean showNotificationNewPosts = request.getHttpRequest().isPartSet("show-notification-new-posts");
				currentSone.getOptions().setShowNewPostNotifications(showNotificationNewPosts);
				boolean showNotificationNewReplies = request.getHttpRequest().isPartSet("show-notification-new-replies");
				currentSone.getOptions().setShowNewReplyNotifications(showNotificationNewReplies);
				String showCustomAvatars = request.getHttpRequest().getPartAsStringFailsafe("show-custom-avatars", 32);
				currentSone.getOptions().setShowCustomAvatars(ShowCustomAvatars.valueOf(showCustomAvatars));
				webInterface.getCore().touchConfiguration();
			}
			Integer insertionDelay = parseInt(request.getHttpRequest().getPartAsStringFailsafe("insertion-delay", 16), null);
			if (!preferences.validateInsertionDelay(insertionDelay)) {
				fieldErrors.add("insertion-delay");
			} else {
				preferences.setInsertionDelay(insertionDelay);
			}
			Integer postsPerPage = parseInt(request.getHttpRequest().getPartAsStringFailsafe("posts-per-page", 4), null);
			if (!preferences.validatePostsPerPage(postsPerPage)) {
				fieldErrors.add("posts-per-page");
			} else {
				preferences.setPostsPerPage(postsPerPage);
			}
			Integer imagesPerPage = parseInt(request.getHttpRequest().getPartAsStringFailsafe("images-per-page", 4), null);
			if (!preferences.validateImagesPerPage(imagesPerPage)) {
				fieldErrors.add("images-per-page");
			} else {
				preferences.setImagesPerPage(imagesPerPage);
			}
			Integer charactersPerPost = parseInt(request.getHttpRequest().getPartAsStringFailsafe("characters-per-post", 10), null);
			if (!preferences.validateCharactersPerPost(charactersPerPost)) {
				fieldErrors.add("characters-per-post");
			} else {
				preferences.setCharactersPerPost(charactersPerPost);
			}
			Integer postCutOffLength = parseInt(request.getHttpRequest().getPartAsStringFailsafe("post-cut-off-length", 10), null);
			if (!preferences.validatePostCutOffLength(postCutOffLength)) {
				fieldErrors.add("post-cut-off-length");
			} else {
				preferences.setPostCutOffLength(postCutOffLength);
			}
			boolean requireFullAccess = request.getHttpRequest().isPartSet("require-full-access");
			preferences.setRequireFullAccess(requireFullAccess);
			Integer positiveTrust = parseInt(request.getHttpRequest().getPartAsStringFailsafe("positive-trust", 3), null);
			if (!preferences.validatePositiveTrust(positiveTrust)) {
				fieldErrors.add("positive-trust");
			} else {
				preferences.setPositiveTrust(positiveTrust);
			}
			Integer negativeTrust = parseInt(request.getHttpRequest().getPartAsStringFailsafe("negative-trust", 4), null);
			if (!preferences.validateNegativeTrust(negativeTrust)) {
				fieldErrors.add("negative-trust");
			} else {
				preferences.setNegativeTrust(negativeTrust);
			}
			String trustComment = request.getHttpRequest().getPartAsStringFailsafe("trust-comment", 256);
			if (trustComment.trim().length() == 0) {
				trustComment = null;
			}
			preferences.setTrustComment(trustComment);
			boolean fcpInterfaceActive = request.getHttpRequest().isPartSet("fcp-interface-active");
			preferences.setFcpInterfaceActive(fcpInterfaceActive);
			Integer fcpFullAccessRequiredInteger = parseInt(request.getHttpRequest().getPartAsStringFailsafe("fcp-full-access-required", 1), preferences.getFcpFullAccessRequired().ordinal());
			FullAccessRequired fcpFullAccessRequired = FullAccessRequired.values()[fcpFullAccessRequiredInteger];
			preferences.setFcpFullAccessRequired(fcpFullAccessRequired);
			webInterface.getCore().touchConfiguration();
			if (fieldErrors.isEmpty()) {
				throw new RedirectException(getPath());
			}
			templateContext.set("fieldErrors", fieldErrors);
		}
		if (currentSone != null) {
			templateContext.set("auto-follow", currentSone.getOptions().isAutoFollow());
			templateContext.set("enable-sone-insert-notifications", currentSone.getOptions().isSoneInsertNotificationEnabled());
			templateContext.set("show-notification-new-sones", currentSone.getOptions().isShowNewSoneNotifications());
			templateContext.set("show-notification-new-posts", currentSone.getOptions().isShowNewPostNotifications());
			templateContext.set("show-notification-new-replies", currentSone.getOptions().isShowNewReplyNotifications());
			templateContext.set("show-custom-avatars", currentSone.getOptions().getShowCustomAvatars().name());
		}
		templateContext.set("insertion-delay", preferences.getInsertionDelay());
		templateContext.set("posts-per-page", preferences.getPostsPerPage());
		templateContext.set("images-per-page", preferences.getImagesPerPage());
		templateContext.set("characters-per-post", preferences.getCharactersPerPost());
		templateContext.set("post-cut-off-length", preferences.getPostCutOffLength());
		templateContext.set("require-full-access", preferences.isRequireFullAccess());
		templateContext.set("positive-trust", preferences.getPositiveTrust());
		templateContext.set("negative-trust", preferences.getNegativeTrust());
		templateContext.set("trust-comment", preferences.getTrustComment());
		templateContext.set("fcp-interface-active", preferences.isFcpInterfaceActive());
		templateContext.set("fcp-full-access-required", preferences.getFcpFullAccessRequired().ordinal());
	}

}
