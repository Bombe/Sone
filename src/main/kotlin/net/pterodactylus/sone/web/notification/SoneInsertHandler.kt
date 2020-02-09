/**
 * Sone - SoneInsertHandler.kt - Copyright © 2020 David ‘Bombe’ Roden
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
import net.pterodactylus.sone.data.*
import net.pterodactylus.util.notify.*
import javax.inject.*

/**
 * Handler for all notifications concerning Sone-insert events.
 */
class SoneInsertHandler @Inject constructor(private val notificationManager: NotificationManager, private val soneNotifications: SoneInsertNotificationSupplier) {

	@Subscribe
	fun soneInserting(event: SoneInsertingEvent) {
		showNotification(event.sone, "inserting")
	}

	@Subscribe
	fun soneInserted(event: SoneInsertedEvent) {
		showNotification(event.sone, "inserted", "insertDuration" to event.insertDuration / 1000)
	}

	@Subscribe
	fun soneInsertAborted(event: SoneInsertAbortedEvent) {
		showNotification(event.sone, "insert-aborted")
	}

	private fun showNotification(sone: Sone, status: String, vararg templateVariables: Pair<String, Any>) {
		if (sone.options.isSoneInsertNotificationEnabled) {
			soneNotifications(sone).let { notification ->
				notification["soneStatus"] = status
				templateVariables.forEach { notification[it.first] = it.second }
				notificationManager.addNotification(notification)
			}
		}
	}

}

typealias SoneInsertNotificationSupplier = (@JvmSuppressWildcards Sone) -> @JvmSuppressWildcards TemplateNotification
