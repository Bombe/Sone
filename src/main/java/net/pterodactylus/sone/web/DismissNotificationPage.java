/*
 * Sone - DismissNotificationPage.java - Copyright © 2010 David Roden
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

import net.pterodactylus.util.notify.Notification;
import net.pterodactylus.util.template.Template;

/**
 * Page that lets the user dismiss a notification.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DismissNotificationPage extends SoneTemplatePage {

	/**
	 * Creates a new “dismiss notifcation” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public DismissNotificationPage(Template template, WebInterface webInterface) {
		super("dismissNotification.html", template, "Page.DismissNotification.Title", webInterface);
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(Request request, Template template) throws RedirectException {
		super.processTemplate(request, template);
		String notificationId = request.getHttpRequest().getPartAsStringFailsafe("notification", 36);
		Notification notification = webInterface.getNotifications().getNotification(notificationId);
		if ((notification != null) && notification.isDismissable()) {
			notification.dismiss();
		}
		String returnPage = request.getHttpRequest().getPartAsStringFailsafe("returnPage", 256);
		throw new RedirectException(returnPage);
	}

}
