/*
 * Sone - GetNotificationsAjaxPage.java - Copyright © 2010 David Roden
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.util.json.JsonArray;
import net.pterodactylus.util.json.JsonObject;
import net.pterodactylus.util.notify.Notification;

/**
 * Returns all changed notifications.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetNotificationsAjaxPage extends JsonPage {

	/**
	 * TODO
	 *
	 * @param webInterface
	 */
	public GetNotificationsAjaxPage(WebInterface webInterface) {
		super("ajax/getNotifications.ajax", webInterface);
	}

	//
	// JSONPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(Request request) {
		List<Notification> notifications = new ArrayList<Notification>(webInterface.getNotifications().getChangedNotifications());
		Collections.sort(notifications, Notification.LAST_UPDATED_TIME_SORTER);
		JsonObject result = createSuccessJsonObject();
		JsonArray jsonNotifications = new JsonArray();
		for (Notification notification : notifications) {
			JsonObject jsonNotification = new JsonObject();
			jsonNotification.put("id", notification.getId());
			jsonNotification.put("text", notification.toString());
			jsonNotification.put("createdTime", notification.getCreatedTime());
			jsonNotification.put("lastUpdatedTime", notification.getLastUpdatedTime());
			jsonNotification.put("dismissable", notification.isDismissable());
			jsonNotifications.add(jsonNotification);
		}
		return result.put("notifications", jsonNotifications);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean needsFormPassword() {
		return false;
	}

}
