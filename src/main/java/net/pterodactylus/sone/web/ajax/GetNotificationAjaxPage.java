/*
 * Sone - GetNotificationAjaxPage.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.web.ajax;

import java.io.IOException;
import java.io.StringWriter;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.main.SonePlugin;
import net.pterodactylus.sone.notify.ListNotification;
import net.pterodactylus.sone.notify.ListNotificationFilters;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.util.json.JsonObject;
import net.pterodactylus.util.notify.Notification;
import net.pterodactylus.util.notify.TemplateNotification;
import net.pterodactylus.util.template.TemplateContext;

/**
 * The “get notification” AJAX handler returns a number of rendered
 * notifications.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetNotificationAjaxPage extends JsonPage {

	/**
	 * Creates a new “get notification” AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public GetNotificationAjaxPage(WebInterface webInterface) {
		super("getNotification.ajax", webInterface);
	}

	//
	// JSONPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean needsFormPassword() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean requiresLogin() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected JsonObject createJsonObject(Request request) {
		String[] notificationIds = request.getHttpRequest().getParam("notifications").split(",");
		JsonObject jsonNotifications = new JsonObject();
		Sone currentSone = getCurrentSone(request.getToadletContext(), false);
		for (String notificationId : notificationIds) {
			Notification notification = webInterface.getNotifications().getNotification(notificationId);
			if ("new-post-notification".equals(notificationId)) {
				notification = ListNotificationFilters.filterNewPostNotification((ListNotification<Post>) notification, currentSone, false);
			} else if ("new-reply-notification".equals(notificationId)) {
				notification = ListNotificationFilters.filterNewReplyNotification((ListNotification<Reply>) notification, currentSone);
			}
			if (notification == null) {
				// TODO - show error
				continue;
			}
			jsonNotifications.put(notificationId, createJsonNotification(request, notification));
		}
		return createSuccessJsonObject().put("notifications", jsonNotifications);
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Creates a JSON object from the given notification.
	 *
	 * @param request
	 *            The request to load the session from
	 * @param notification
	 *            The notification to create a JSON object
	 * @return The JSON object
	 */
	private JsonObject createJsonNotification(Request request, Notification notification) {
		JsonObject jsonNotification = new JsonObject();
		jsonNotification.put("id", notification.getId());
		StringWriter notificationWriter = new StringWriter();
		try {
			if (notification instanceof TemplateNotification) {
				TemplateContext templateContext = webInterface.getTemplateContextFactory().createTemplateContext().mergeContext(((TemplateNotification) notification).getTemplateContext());
				templateContext.set("core", webInterface.getCore());
				templateContext.set("currentSone", webInterface.getCurrentSone(request.getToadletContext(), false));
				templateContext.set("localSones", webInterface.getCore().getLocalSones());
				templateContext.set("request", request);
				templateContext.set("currentVersion", SonePlugin.VERSION);
				templateContext.set("hasLatestVersion", webInterface.getCore().getUpdateChecker().hasLatestVersion());
				templateContext.set("latestEdition", webInterface.getCore().getUpdateChecker().getLatestEdition());
				templateContext.set("latestVersion", webInterface.getCore().getUpdateChecker().getLatestVersion());
				templateContext.set("latestVersionTime", webInterface.getCore().getUpdateChecker().getLatestVersionDate());
				templateContext.set("notification", notification);
				((TemplateNotification) notification).render(templateContext, notificationWriter);
			} else {
				notification.render(notificationWriter);
			}
		} catch (IOException ioe1) {
			/* StringWriter never throws, ignore. */
		}
		jsonNotification.put("text", notificationWriter.toString());
		jsonNotification.put("createdTime", notification.getCreatedTime());
		jsonNotification.put("lastUpdatedTime", notification.getLastUpdatedTime());
		jsonNotification.put("dismissable", notification.isDismissable());
		return jsonNotification;
	}

}
