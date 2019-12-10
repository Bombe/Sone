/**
 * Sone - SoneLockedHandler.kt - Copyright © 2019 David ‘Bombe’ Roden
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
import net.pterodactylus.sone.notify.*
import net.pterodactylus.util.notify.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*

/**
 * Handler for [SoneLockedEvent]s and [SoneUnlockedEvent]s that can schedule notifications after
 * a certain timeout.
 */
class SoneLockedHandler(private val notificationManager: NotificationManager, private val notification: ListNotification<Sone>, private val executor: ScheduledExecutorService) {

	private val future: AtomicReference<ScheduledFuture<*>> = AtomicReference()

	@Subscribe
	fun soneLocked(soneLockedEvent: SoneLockedEvent) {
		synchronized(future) {
			future.get()?.also { cancelPreviousFuture(it, soneLockedEvent.sone) }
			future.set(executor.schedule(showNotification(soneLockedEvent.sone), 5, TimeUnit.MINUTES))
		}
	}

	@Subscribe
	fun soneUnlocked(soneUnlockedEvent: SoneUnlockedEvent) {
		synchronized(future) {
			future.get()?.also { cancelPreviousFuture(it, soneUnlockedEvent.sone) }
		}
	}

	private fun cancelPreviousFuture(future: ScheduledFuture<*>, sone: Sone) {
		notification.remove(sone)
		future.cancel(true)
	}

	private fun showNotification(sone: Sone): () -> Unit = {
		notification.add(sone)
		notificationManager.addNotification(notification)
	}

}
