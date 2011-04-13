/*
 * Sone - NotificationManagerAccessor.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.notify.ListNotificationFilters;
import net.pterodactylus.util.notify.Notification;
import net.pterodactylus.util.notify.NotificationManager;
import net.pterodactylus.util.template.ReflectionAccessor;
import net.pterodactylus.util.template.TemplateContext;

/**
 * Adds additional properties to a {@link NotificationManager}.
 * <dl>
 * <dd>all</dd>
 * <dt>Returns all notifications, sorted by creation time, oldest first.</dt>
 * <dd>new</dd>
 * <dt>Returns all changed notifications, sorted by last updated time, newest
 * first.</dt>
 * </dl>
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class NotificationManagerAccessor extends ReflectionAccessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(TemplateContext templateContext, Object object, String member) {
		NotificationManager notificationManager = (NotificationManager) object;
		if ("all".equals(member)) {
			List<Notification> notifications = ListNotificationFilters.filterNotifications(new ArrayList<Notification>(notificationManager.getNotifications()), (Sone) templateContext.get("currentSone"));
			Collections.sort(notifications, Notification.CREATED_TIME_SORTER);
			return notifications;
		}
		return super.get(templateContext, object, member);
	}

}
