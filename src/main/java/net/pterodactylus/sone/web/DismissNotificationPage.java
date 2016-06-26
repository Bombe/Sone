/*
 * Sone - DismissNotificationPage.java - Copyright © 2010–2016 David Roden
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

import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.notify.Notification;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

import com.google.common.base.Optional;

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
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		String notificationId = request.getHttpRequest().getPartAsStringFailsafe("notification", 36);
		Optional<Notification> notification = webInterface.getNotification(notificationId);
		if (notification.isPresent() && notification.get().isDismissable()) {
			notification.get().dismiss();
		}
		String returnPage = request.getHttpRequest().getPartAsStringFailsafe("returnPage", 256);
		throw new RedirectException(returnPage);
	}

}
