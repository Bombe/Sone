/*
 * Sone - DismissNotificationAjaxPage.java - Copyright © 2010 David Roden
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

import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.util.json.JsonObject;
import net.pterodactylus.util.notify.Notification;

/**
 * AJAX page that lets the user dismiss a notification.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DismissNotificationAjaxPage extends JsonPage {

	/**
	 * Creates a new “dismiss notification” AJAX handler.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public DismissNotificationAjaxPage(WebInterface webInterface) {
		super("ajax/dismissNotification.ajax", webInterface);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(Request request) {
		String notificationId = request.getHttpRequest().getParam("notification");
		Notification notification = webInterface.getNotifications().getNotification(notificationId);
		if (notification == null) {
			return createErrorJsonObject("invalid-notification-id");
		}
		if (!notification.isDismissable()) {
			return createErrorJsonObject("not-dismissable");
		}
		notification.dismiss();
		return createSuccessJsonObject();
	}

}
