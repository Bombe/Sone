/**
 * Sone - WebOfTrustPinger.kt - Copyright © 2019–2020 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.freenet.wot

import com.google.common.eventbus.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.freenet.plugin.*
import net.pterodactylus.sone.utils.*
import java.util.concurrent.atomic.*
import java.util.function.*
import javax.inject.*

/**
 * [Runnable] that is scheduled via an [Executor][java.util.concurrent.Executor],
 * checks whether the web of trust plugin can be communicated with, sends
 * events if its status changes and reschedules itself.
 */
class WebOfTrustPinger @Inject constructor(
		private val eventBus: EventBus,
		@Named("webOfTrustReacher") private val webOfTrustReacher: Runnable,
		@Named("webOfTrustReschedule") private val reschedule: Consumer<Runnable>) : Runnable {

	private val lastState = AtomicBoolean(false)

	override fun run() {
		try {
			webOfTrustReacher()
			if (!lastState.get()) {
				eventBus.post(WebOfTrustAppeared())
				lastState.set(true)
			}
		} catch (e: PluginException) {
			if (lastState.get()) {
				eventBus.post(WebOfTrustDisappeared())
				lastState.set(false)
			}
		}
		reschedule(this)
	}

}
