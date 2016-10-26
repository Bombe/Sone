/*
 * Sone - GetNotificationsAjaxPage.java - Copyright © 2011–2016 David Roden
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

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.main.SonePlugin;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.notify.Notification;
import net.pterodactylus.util.notify.TemplateNotification;
import net.pterodactylus.util.template.TemplateContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * AJAX handler to return all current notifications.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetNotificationsAjaxPage extends JsonPage {

	/**
	 * Creates a new “get notifications” AJAX handler.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public GetNotificationsAjaxPage(WebInterface webInterface) {
		super("getNotifications.ajax", webInterface);
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
	protected JsonReturnObject createJsonObject(FreenetRequest request) {
		Sone currentSone = getCurrentSone(request.getToadletContext(), false);
		List<Notification> notifications = new ArrayList<Notification>(webInterface.getNotifications(currentSone));
		Collections.sort(notifications, Notification.CREATED_TIME_SORTER);
		ArrayNode jsonNotifications = new ArrayNode(instance);
		for (Notification notification : notifications) {
			jsonNotifications.add(createJsonNotification(request, notification));
		}
		return createSuccessJsonObject().put("notificationHash", notifications.hashCode()).put("notifications", jsonNotifications).put("options", createJsonOptions(currentSone));
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
	private JsonNode createJsonNotification(FreenetRequest request, Notification notification) {
		ObjectNode jsonNotification = new ObjectNode(instance);
		jsonNotification.put("id", notification.getId());
		StringWriter notificationWriter = new StringWriter();
		try {
			if (notification instanceof TemplateNotification) {
				TemplateContext templateContext = webInterface.getTemplateContextFactory().createTemplateContext().mergeContext(((TemplateNotification) notification).getTemplateContext());
				templateContext.set("core", webInterface.getCore());
				templateContext.set("currentSone", webInterface.getCurrentSone(request.getToadletContext(), false));
				templateContext.set("localSones", webInterface.getCore().getLocalSones());
				templateContext.set("request", request);
				templateContext.set("currentVersion", SonePlugin.getPluginVersion());
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

	/**
	 * Creates a JSON object that contains all options that are currently in
	 * effect for the given Sone (or overall, if the given Sone is {@code null}
	 * ).
	 *
	 * @param currentSone
	 *            The current Sone (may be {@code null})
	 * @return The current options
	 */
	private static JsonNode createJsonOptions(Sone currentSone) {
		ObjectNode options = new ObjectNode(instance);
		if (currentSone != null) {
			options.put("ShowNotification/NewSones", currentSone.getOptions().isShowNewSoneNotifications());
			options.put("ShowNotification/NewPosts", currentSone.getOptions().isShowNewPostNotifications());
			options.put("ShowNotification/NewReplies", currentSone.getOptions().isShowNewReplyNotifications());
		}
		return options;
	}

}
