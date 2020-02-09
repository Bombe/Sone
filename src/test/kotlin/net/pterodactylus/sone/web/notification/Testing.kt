/**
 * Sone - Testing.kt - Copyright © 2019–2020 David ‘Bombe’ Roden
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

import net.pterodactylus.util.notify.*
import java.io.*
import java.util.concurrent.*

/** Information about a scheduled runnable. */
data class Scheduled(val command: Runnable, val delay: Long, val timeUnit: TimeUnit, val future: ScheduledFuture<*>)

/**
 * [ScheduledThreadPoolExecutor] extension that stores parameters and return
 * values for the [ScheduledThreadPoolExecutor.schedule] method.
 */
class TestScheduledThreadPoolExecutor : ScheduledThreadPoolExecutor(1) {

	val scheduleds = mutableListOf<Scheduled>()

	override fun schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*> =
			super.schedule(command, delay, unit)
					.also { scheduleds += Scheduled(command, delay, unit, it) }

}

fun NotificationManager.firstStart() {
	addNotification(object : AbstractNotification("first-start-notification") {
		override fun render(writer: Writer?) = Unit
	})
}
