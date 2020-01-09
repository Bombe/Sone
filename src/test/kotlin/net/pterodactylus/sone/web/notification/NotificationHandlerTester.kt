/**
 * Sone - NotificationHandlerTester.kt - Copyright © 2019 David ‘Bombe’ Roden
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
import net.pterodactylus.util.notify.*

/**
 * Helper for testing event handlers that deal with notifications. It contains
 * a notification manager and an [event bus][EventBus] and automatically
 * registers the created handler on the event bus.
 *
 * ```
 * val notification = SomeNotification()
 * val notificationTester = NotificationTester { SomeHandler(it, notification) }
 *
 * fun test() {
 *     notificationTester.sendEvent(SomeEvent())
 *     assertThat(notificationTester.elements, hasItem(notification))
 * }
 * ```
 */
@Suppress("UnstableApiUsage")
class NotificationHandlerTester(createHandler: (NotificationManager) -> Any) {

	private val eventBus = EventBus()
	private val notificationManager = NotificationManager()

	/** Returns all notifications of the notification manager. */
	val notifications: Set<Notification>
		get() = notificationManager.notifications

	init {
		eventBus.register(createHandler(notificationManager))
	}

	/** Sends an event to the event bus. */
	fun sendEvent(event: Any) = eventBus.post(event)

	/** Sets the first-start notification on the notification manager. */
	fun firstStart() = notificationManager.firstStart()

}
