/**
 * Sone - NewVersionHandler.kt - Copyright © 2019 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.web.notification

import com.google.common.eventbus.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.util.notify.*

/**
 * Handler for the “new version” notification.
 */
class NewVersionHandler(private val notificationManager: NotificationManager, private val notification: TemplateNotification) {

	@Subscribe
	fun newVersionFound(updateFoundEvent: UpdateFoundEvent) {
		notification.set("latestVersion", updateFoundEvent.version)
		notification.set("releaseTime", updateFoundEvent.releaseTime)
		notification.set("latestEdition", updateFoundEvent.latestEdition)
		notification.set("disruptive", updateFoundEvent.isDisruptive)
		notificationManager.addNotification(notification)
	}

}
