/**
 * Sone - Notifications.kt - Copyright © 2019 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.notify

import net.pterodactylus.util.notify.*

/**
 * Returns whether the notification manager contains a notification with the given ID.
 */
operator fun NotificationManager.contains(id: String) =
		getNotification(id) != null

/**
 * Returns whether the notification manager currently has a “first start” notification.
 */
fun NotificationManager.hasFirstStartNotification() =
		"first-start-notification" in this
